// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/components/brave_today/browser/brave_news_controller.h"


#include "base/bind.h"
#include "base/time/time.h"
#include "base/values.h"
#include "brave/common/pref_names.h" // TODO: move to this component
#include "brave/components/brave_private_cdn/private_cdn_helper.h"
#include "brave/components/brave_today/browser/urls.h"
#include "brave/components/brave_today/browser/feed_parsing.h"
#include "brave/components/brave_today/common/brave_news.mojom-forward.h"
#include "brave/components/brave_today/common/brave_news.mojom.h"
#include "brave/components/brave_today/common/brave_news.mojom-shared.h"
#include "net/base/escape.h"
#include "net/base/load_flags.h"
#include "net/base/url_util.h"
#include "services/network/public/cpp/shared_url_loader_factory.h"
#include "services/network/public/cpp/simple_url_loader.h"

namespace {
  net::NetworkTrafficAnnotationTag GetNetworkTrafficAnnotationTag() {
  return net::DefineNetworkTrafficAnnotation("brave_news_controller", R"(
      semantics {
        sender: "Brave News Controller"
        description:
          "This controller is used to fetch brave news feeds and publisher lists."
        trigger:
          "Triggered by uses of the Brave News feature."
        data:
          "Article JSON"
        destination: WEBSITE
      }
      policy {
        cookies_allowed: NO
        setting:
          "You can enable or disable this feature on the New Tab Page customization."
        policy_exception_justification:
          "Not implemented."
      }
    )");
}

}  // namespace

namespace brave_news {
using std::move;

BraveNewsController::BraveNewsController(PrefService* prefs,
    scoped_refptr<network::SharedURLLoaderFactory> url_loader_factory)
    : prefs_(prefs),
      api_request_helper_(GetNetworkTrafficAnnotationTag(), url_loader_factory),
      weak_ptr_factory_(this) {
  DCHECK(prefs);
  // Set up preference listeners
  pref_change_registrar_.Init(prefs);
  pref_change_registrar_.Add(kNewTabPageShowToday,
      base::BindRepeating(&BraveNewsController::ConditionallyStartOrStopTimer,
          base::Unretained(this)));
  pref_change_registrar_.Add(kBraveTodayOptedIn,
      base::BindRepeating(&BraveNewsController::ConditionallyStartOrStopTimer,
          base::Unretained(this)));
  // Monitor kBraveTodaySources and update feed / publisher cache
  // Start timer of updating feeds, if applicable
  ConditionallyStartOrStopTimer();
}

BraveNewsController::~BraveNewsController() = default;

mojo::PendingRemote<mojom::BraveNewsController> BraveNewsController::MakeRemote(
    ) {
  mojo::PendingRemote<mojom::BraveNewsController> remote;
  receivers_.Add(this, remote.InitWithNewPipeAndPassReceiver());
  return remote;
}

void BraveNewsController::Bind(
    mojo::PendingReceiver<mojom::BraveNewsController> receiver) {
  receivers_.Add(this, std::move(receiver));
}

void BraveNewsController::GetFeed(GetFeedCallback callback) {
  GetOrFetchFeed(std::move(callback));
}

void BraveNewsController::GetPublishers(GetPublishersCallback callback){
  GetOrFetchPublishers(std::move(callback));
}

void BraveNewsController::GetImageData(const GURL& padded_image_url,
    GetImageDataCallback callback){
  // Handler url download response
  auto onPaddedImageResponse = base::BindOnce(
    [](GetImageDataCallback callback, const int status, const std::string& body,
          const base::flat_map<std::string, std::string>& headers) {
      // Attempt to remove byte padding
      base::StringPiece body_payload(body.data(), body.size());
      if (status < 200 || status >= 300 ||
          !brave::PrivateCdnHelper::GetInstance()->RemovePadding(
                &body_payload)) {
        // Byte padding removal failed
        absl::optional<std::vector<uint8_t>> args;
        std::move(callback).Run(std::move(args));
      }
      // Unpadding was successful, uint8Array will be easier to move over mojom
      std::vector<uint8_t> image_bytes(body_payload.begin(), body_payload.end());
      std::move(callback).Run(image_bytes);
    }, std::move(callback));
  api_request_helper_.Request("GET", padded_image_url, "", "", true,
      std::move(onPaddedImageResponse));
}

void BraveNewsController::SetPublisherPref(
    const std::string& publisher_id,
    mojom::UserEnabled new_status){

}

void BraveNewsController::ClearPrefs(ClearPrefsCallback callback){

}

void BraveNewsController::IsFeedUpdateAvailable(
    const std::string& displayed_feed_hash,
    IsFeedUpdateAvailableCallback callback){
  // Get cached feed and compare hash
  auto onFeed = base::BindOnce(
    [](BraveNewsController* controller, IsFeedUpdateAvailableCallback callback,
      std::string original_hash, mojom::FeedPtr feed) {
        std::move(callback).Run(original_hash != feed->hash);
    }, base::Unretained(this), std::move(callback), displayed_feed_hash);
  GetOrFetchFeed(std::move(onFeed));
}

void BraveNewsController::CheckForFeedsUpdate() {

}

void BraveNewsController::CheckForSourcesUpdate() {

}

void BraveNewsController::GetOrFetchFeed(GetFeedCallback callback) {
  if (!current_feed_.hash.empty()) {
    auto clone = current_feed_.Clone();
    std::move(callback).Run(std::move(clone));
    return;
  }
  UpdateFeed(std::move(callback));
}
void BraveNewsController::GetOrFetchPublishers(GetPublishersCallback callback) {
  // Use memory cache if available
  if (!publishers_.empty()) {
    ProvidePublishersClone(std::move(callback));
    // std::move(callback).Run(publishers_);
    return;
  }
  // TODO: make sure only one at a time
  // fetching_feed_future_.wait();
  // fetching_feed_future_.get()
  // Re-check memory cache now that possible request is finished
  // if (!publishers_.empty()) {
  //   std::move(callback).Run(publishers_);
  //   return;
  // }
  // Perform fetch
  UpdatePublishers(std::move(callback));
}

void BraveNewsController::UpdateFeed(GetFeedCallback callback) {
  GURL feed_url("https://" + brave_today::GetHostname() + "/brave-today/feed." + brave_today::GetRegionUrlPart() + "json");
  auto onRequest = base::BindOnce(
    [](BraveNewsController* controller, GetFeedCallback callback,
      const int status, const std::string& body,
      const base::flat_map<std::string, std::string>& headers) {
        // TODO(petemill): handle bad response
        // TODO(petemill): avoid callback hell
        auto onPublishers = base::BindOnce(
          [](BraveNewsController* controller, GetFeedCallback callback,
            const std::string& body, Publishers publishers) {
              LOG(ERROR) << "Got feed AND publishers!";
              // TODO(petemill): Handle no publishers
              // Reset feed data
              // controller->current_feed_.featured_article = nullptr;
              // controller->current_feed_.hash = "";
              // controller->current_feed_.pages.clear();
              ParseFeed(body, &publishers, &controller->current_feed_);
              auto clone = controller->current_feed_.Clone();
              std::move(callback).Run(std::move(clone));
            }, base::Unretained(controller),
              std::move(callback), std::move(body));
        controller->GetOrFetchPublishers(std::move(onPublishers));
    }, base::Unretained(this), std::move(callback));
  api_request_helper_.Request("GET", feed_url, "", "", true,
        std::move(onRequest));
}

void BraveNewsController::UpdatePublishers(GetPublishersCallback callback) {
  GURL sources_url("https://" + brave_today::GetHostname() + "/sources." + brave_today::GetRegionUrlPart() + "json");
  auto onRequest = base::BindOnce(
    [](BraveNewsController* controller, GetPublishersCallback callback,
      const int status, const std::string& body,
      const base::flat_map<std::string, std::string>& headers) {
          // TODO(petemill): handle bad status or response
          Publishers publisher_list;
          ParsePublisherList(body, &publisher_list);
          // Add user enabled statuses
          const base::DictionaryValue* publisher_prefs =
              controller->prefs_->GetDictionary(kBraveTodaySources);
          for (auto kv : publisher_prefs->DictItems()) {
            auto publisher_id = kv.first;
            auto is_user_enabled = kv.second.GetIfBool();
            if (publisher_list.contains(publisher_id)) {
              // publisher_list.find(publisher_id)->second->user_enabled_status =
              publisher_list[publisher_id]->user_enabled_status =
                (is_user_enabled ? brave_news::mojom::UserEnabled::ENABLED
                                : brave_news::mojom::UserEnabled::DISABLED);
            }
          }
          // Set memory cache
          controller->publishers_ = std::move(publisher_list);
          controller->ProvidePublishersClone(std::move(callback));
          // TODO: notify other callbacks / Release lock
          // controller->fetching_sources_lock_.Release();
          // std::move(callback).Run(controller->publishers_);
    }, base::Unretained(this), std::move(callback));
  api_request_helper_.Request("GET", sources_url, "", "", true,
        std::move(onRequest));
}

void BraveNewsController::ProvidePublishersClone(GetPublishersCallback cb) {
  Publishers clone;
  for (auto const& kv : publishers_) {
    clone.insert_or_assign(kv.first, kv.second->Clone());
  }
  std::move(cb).Run(std::move(clone));
}

void BraveNewsController::ConditionallyStartOrStopTimer() {
  // Refresh data on an interval only if Brave News is enabled
  bool should_show = prefs_->GetBoolean(kNewTabPageShowToday);
  bool opted_in = prefs_->GetBoolean(kBraveTodayOptedIn);
  bool is_enabled = (should_show && opted_in);
  if (is_enabled) {
    timer_feed_update_.Stop();
    timer_publishers_update_.Stop();
  } else {
    timer_feed_update_.Start(FROM_HERE, base::TimeDelta::FromHours(3),
        this, &BraveNewsController::CheckForFeedsUpdate);
    timer_publishers_update_.Start(FROM_HERE, base::TimeDelta::FromDays(1),
        this, &BraveNewsController::CheckForSourcesUpdate);
  }

}

}  // namespace brave_news

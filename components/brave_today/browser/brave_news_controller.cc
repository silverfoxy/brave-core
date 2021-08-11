// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/components/brave_today/browser/brave_news_controller.h"

#include "base/bind.h"
#include "base/time/time.h"
#include "brave/common/pref_names.h" // TODO: move to this component
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

}

void BraveNewsController::CheckForFeedsUpdate() {

}

void BraveNewsController::CheckForSourcesUpdate() {

}

void BraveNewsController::GetOrFetchFeed(GetFeedCallback callback) {

}
void BraveNewsController::GetOrFetchPublishers(GetPublishersCallback callback) {

}
void BraveNewsController::UpdateFeed() {

}
void BraveNewsController::UpdatePublishers() {

}
Publishers BraveNewsController::BuildPublishers(
    std::vector<mojom::PublisherPtr> publishers) {
  Publishers result;
  return result;
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

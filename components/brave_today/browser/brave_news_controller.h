// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#ifndef BRAVE_COMPONENTS_BRAVE_TODAY_BROWSER_BRAVE_NEWS_CONTROLLER_H_
#define BRAVE_COMPONENTS_BRAVE_TODAY_BROWSER_BRAVE_NEWS_CONTROLLER_H_

#include <string>

#include "base/containers/flat_map.h"
#include "base/timer/timer.h"
#include "brave/components/api_request_helper/api_request_helper.h"
#include "brave/components/brave_today/common/brave_news.mojom-forward.h"
#include "brave/components/brave_today/common/brave_news.mojom.h"
#include "components/keyed_service/core/keyed_service.h"
#include "components/prefs/pref_change_registrar.h"
#include "components/prefs/pref_service.h"
#include "mojo/public/cpp/bindings/pending_remote.h"
#include "mojo/public/cpp/bindings/receiver_set.h"
#include "mojo/public/cpp/bindings/remote.h"

class PrefService;

namespace brave {
class BraveP3AService;
}  // namespace brave

namespace brave_ads {
class AdsService;
}  // namespace brave_ads

namespace user_prefs {
class PrefRegistrySyncable;
}  // namespace user_prefs

namespace brave_news {

using Publishers = base::flat_map<std::string, mojom::PublisherPtr>;

// Browser-side handler for Brave News mojom API, 1 per profile
class BraveNewsController : public KeyedService, public mojom::BraveNewsController {
 public:
  // static void RegisterProfilePrefs(user_prefs::PrefRegistrySyncable* registry);

  BraveNewsController(PrefService* prefs,
    scoped_refptr<network::SharedURLLoaderFactory> url_loader_factory);
  ~BraveNewsController() override;
  BraveNewsController(const BraveNewsController&) = delete;
  BraveNewsController& operator=(const BraveNewsController&) = delete;

  mojo::PendingRemote<mojom::BraveNewsController> MakeRemote();
  void Bind(mojo::PendingReceiver<mojom::BraveNewsController> receiver);

  // mojom::BraveNewsController
  void GetFeed(GetFeedCallback callback) override;
  void GetPublishers(GetPublishersCallback callback) override;
  void GetImageData(
      const GURL& padded_image_url, GetImageDataCallback callback) override;
  void SetPublisherPref(
      const std::string& publisher_id,
      mojom::UserEnabled new_status) override;
  void ClearPrefs(ClearPrefsCallback callback) override;
  void IsFeedUpdateAvailable(
      const std::string& displayed_feed_hash,
      IsFeedUpdateAvailableCallback callback) override;

 private:
  void ConditionallyStartOrStopTimer();
  void CheckForFeedsUpdate();
  void CheckForSourcesUpdate();
  void GetOrFetchFeed(GetFeedCallback callback);
  void GetOrFetchPublishers(GetPublishersCallback callback);
  void UpdateFeed();
  void UpdatePublishers();
  Publishers BuildPublishers(std::vector<mojom::PublisherPtr> publishers);

  PrefService* prefs_;
  api_request_helper::APIRequestHelper api_request_helper_;

  PrefChangeRegistrar pref_change_registrar_;
  base::RepeatingTimer timer_feed_update_;
  base::RepeatingTimer timer_publishers_update_;

  mojom::Feed current_feed_;
  Publishers publishers_;

  mojo::ReceiverSet<mojom::BraveNewsController> receivers_;
  base::WeakPtrFactory<BraveNewsController> weak_ptr_factory_;
};

}  // namespace brave_news

#endif  // BRAVE_COMPONENTS_BRAVE_TODAY_BROWSER_BRAVE_NEWS_CONTROLLER_H_

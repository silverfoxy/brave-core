/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_BROWSER_BRAVE_ADS_BRAVE_ADS_HOST_H_
#define BRAVE_BROWSER_BRAVE_ADS_BRAVE_ADS_HOST_H_

#include <string>

#include "base/scoped_observation.h"
#include "base/sequence_checker.h"
#include "brave/browser/extensions/api/brave_action_api.h"
#include "brave/components/brave_ads/common/brave_ads_host.mojom.h"
#include "brave/components/brave_rewards/browser/rewards_service.h"
#include "brave/components/brave_rewards/browser/rewards_service_observer.h"
#include "content/public/browser/web_contents_observer.h"

class Browser;
class Profile;

namespace content {
class WebContents;
}

namespace brave_ads {

class BraveAdsHost : public brave_ads::mojom::BraveAdsHost,
                     public content::WebContentsObserver,
                     public brave_rewards::RewardsServiceObserver,
                     public extensions::BraveActionAPI::ActionUIObserver {
 public:
  BraveAdsHost(Profile* profile, content::WebContents* web_contents);
  BraveAdsHost(const BraveAdsHost&) = delete;
  BraveAdsHost& operator=(const BraveAdsHost&) = delete;
  ~BraveAdsHost() override;

  // brave_ads::mojom::BraveAdsHost
  void RequestAdsEnabled(RequestAdsEnabledCallback callback) override;

  // content::WebContentsObserver
  void WebContentsDestroyed() override;

  // brave_rewards::RewardsServiceObserver
  void OnRewardsEnabled() override;
  void OnAdsEnabled(brave_rewards::RewardsService* rewards_service,
                    bool ads_enabled) override;

  // extensions::BraveActionAPI::ActionUIObserver
  void OnActionUIClosed(Browser* browser,
                        const std::string& extension_id) override;

 private:
  bool ShowRewardsPopup(Browser* browser);
  void Reset();

  Profile* profile_;
  content::WebContents* web_contents_;
  RequestAdsEnabledCallback callback_;
  base::ScopedObservation<brave_rewards::RewardsService,
                          brave_rewards::RewardsServiceObserver>
      rewards_service_observation_{this};
  base::ScopedObservation<extensions::BraveActionAPI,
                          extensions::BraveActionAPI::ActionUIObserver,
                          &extensions::BraveActionAPI::AddActionUIObserver,
                          &extensions::BraveActionAPI::RemoveActionUIObserver>
      action_ui_observation_{this};
  Browser* initiated_browser_ = nullptr;

  SEQUENCE_CHECKER(sequence_checker_);
};

}  // namespace brave_ads

#endif  // BRAVE_BROWSER_BRAVE_ADS_BRAVE_ADS_HOST_H_

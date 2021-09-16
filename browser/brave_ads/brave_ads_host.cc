/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "brave/browser/brave_ads/brave_ads_host.h"

#include <memory>
#include <string>
#include <utility>

#include "brave/browser/brave_ads/ads_service_factory.h"
#include "brave/browser/brave_rewards/rewards_service_factory.h"
#include "brave/browser/extensions/api/brave_action_api.h"
#include "brave/browser/extensions/brave_component_loader.h"
#include "brave/components/brave_ads/browser/ads_service.h"
#include "brave/components/brave_rewards/browser/rewards_service.h"
#include "chrome/browser/extensions/extension_service.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/browser/ui/browser.h"
#include "chrome/browser/ui/browser_finder.h"
#include "content/public/browser/web_contents.h"
#include "extensions/browser/extension_system.h"
#include "extensions/common/constants.h"

namespace brave_ads {

namespace {

constexpr char kAdsEnableRelativeUrl[] = "request_ads_enabled_panel.html";

}  // namespace

BraveAdsHost::BraveAdsHost(content::WebContents* web_contents)
    : web_contents_(web_contents) {
  DCHECK(web_contents_);
}

BraveAdsHost::~BraveAdsHost() {}

void BraveAdsHost::RequestAdsEnabled(RequestAdsEnabledCallback callback) {
  DCHECK(callback);

  Profile* profile =
      Profile::FromBrowserContext(web_contents_->GetBrowserContext());
  DCHECK(profile);

  const AdsService* ads_service = AdsServiceFactory::GetForProfile(profile);
  brave_rewards::RewardsService* rewards_service =
      brave_rewards::RewardsServiceFactory::GetForProfile(profile);
  if (!rewards_service || !ads_service || !ads_service->IsSupportedLocale()) {
    std::move(callback).Run(false);
    return;
  }

  if (ads_service->IsEnabled()) {
    std::move(callback).Run(true);
    return;
  }

  if (callback_) {
    std::move(callback).Run(false);
    return;
  }

  Browser* browser = chrome::FindBrowserWithWebContents(web_contents_);
  if (!browser) {
    std::move(callback).Run(false);
    return;
  }

  callback_ = std::move(callback);

  rewards_service_observation_.Observe(rewards_service);

  if (!ShowRewardsPopup(browser, profile)) {
    RunCallbackAndReset(false);
  }
}

void BraveAdsHost::OnRequestAdsEnabledPopupClosed(bool ads_enabled) {
  // If ads were enabled then do nothing and wait for ads enabled event.
  if (!ads_enabled) {
    RunCallbackAndReset(false);
  }
}

void BraveAdsHost::OnAdsEnabled(brave_rewards::RewardsService* rewards_service,
                                bool ads_enabled) {
  DCHECK(rewards_service);

  RunCallbackAndReset(ads_enabled);
}

bool BraveAdsHost::ShowRewardsPopup(Browser* browser, Profile* profile) {
  auto* extension_service =
      extensions::ExtensionSystem::Get(profile)->extension_service();
  if (!extension_service) {
    return false;
  }

  static_cast<extensions::BraveComponentLoader*>(
      extension_service->component_loader())
      ->AddRewardsExtension();

  std::string error;
  return extensions::BraveActionAPI::ShowActionUI(
      browser, brave_rewards_extension_id,
      std::make_unique<std::string>(kAdsEnableRelativeUrl), &error);
}

void BraveAdsHost::RunCallbackAndReset(bool result) {
  DCHECK(callback_);

  rewards_service_observation_.Reset();

  std::move(callback_).Run(result);
}

}  // namespace brave_ads

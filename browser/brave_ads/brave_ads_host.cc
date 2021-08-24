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

BraveAdsHost::BraveAdsHost(Profile* profile, content::WebContents* web_contents)
    : WebContentsObserver(web_contents),
      profile_(profile),
      web_contents_(web_contents) {
  DCHECK(profile_);
  DCHECK(web_contents_);
}

BraveAdsHost::~BraveAdsHost() {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);
  DCHECK(!callback_);
}

void BraveAdsHost::RequestAdsEnabled(RequestAdsEnabledCallback callback) {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);
  DCHECK(callback);
  const AdsService* ads_service = AdsServiceFactory::GetForProfile(profile_);
  brave_rewards::RewardsService* rewards_service =
      brave_rewards::RewardsServiceFactory::GetForProfile(profile_);
  if (!ads_service || !rewards_service) {
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

  rewards_service->StartProcess(base::DoNothing());
  rewards_service_observation_.Observe(rewards_service);
  action_ui_observation_.Observe(extensions::BraveActionAPI::Get(browser));
  initiated_browser_ = browser;

  if (!ShowRewardsPopup(browser)) {
    Reset();
    std::move(callback_).Run(false);
  }
}

void BraveAdsHost::WebContentsDestroyed() {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);

  Reset();

  if (callback_) {
    std::move(callback_).Run(false);
  }
}

void BraveAdsHost::OnRewardsEnabled() {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);

  Reset();

  if (!callback_) {
    NOTREACHED();
    return;
  }

  std::move(callback_).Run(true);
}

void BraveAdsHost::OnAdsEnabled(brave_rewards::RewardsService* rewards_service,
                                bool ads_enabled) {
  DCHECK(rewards_service);
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);

  Reset();

  if (!callback_) {
    NOTREACHED();
    return;
  }

  std::move(callback_).Run(ads_enabled);
}

void BraveAdsHost::OnActionUIClosed(Browser* browser,
                                    const std::string& extension_id) {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);
  if (extension_id != brave_rewards_extension_id) {
    return;
  }

  const Browser* current_browser =
      chrome::FindBrowserWithWebContents(web_contents_);
  if (browser != current_browser && browser != initiated_browser_) {
    return;
  }

  Reset();

  if (!callback_) {
    NOTREACHED();
    return;
  }

  std::move(callback_).Run(false);
}

bool BraveAdsHost::ShowRewardsPopup(Browser* browser) {
  auto* extension_service =
      extensions::ExtensionSystem::Get(profile_)->extension_service();
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

void BraveAdsHost::Reset() {
  rewards_service_observation_.Reset();
  action_ui_observation_.Reset();
  initiated_browser_ = nullptr;
}

}  // namespace brave_ads

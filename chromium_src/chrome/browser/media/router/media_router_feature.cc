/* Copyright 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "chrome/browser/media/router/media_router_feature.h"

#include "brave/common/pref_names.h"
#include "content/public/browser/browser_context.h"

#define MediaRouterEnabled MediaRouterEnabled_ChromiumImpl
#include "../../../../../../chrome/browser/media/router/media_router_feature.cc"
#undef MediaRouterEnabled

namespace media_router {

bool initialized = false;

void InitializeMediaRouter(content::BrowserContext* context) {
  auto* pref_service = user_prefs::UserPrefs::Get(context);
  auto enabled = pref_service->GetBoolean(kBraveMediaRouter);
  pref_service->SetBoolean(::prefs::kEnableMediaRouter, enabled);
  LOG(ERROR) << "Setting BraveMediaRouter: " << enabled;
  initialized = true;
}

bool MediaRouterEnabled(content::BrowserContext* context) {
#if defined(OS_ANDROID)
  return MediaRouterEnabled_ChromiumImpl(context);
#endif
  if (!initialized) {
    InitializeMediaRouter(context);
  }
  const PrefService::Preference* pref = GetMediaRouterPref(context);
  CHECK(pref->GetValue()->is_bool());
  return pref->GetValue()->GetBool();
}

}  // namespace media_router

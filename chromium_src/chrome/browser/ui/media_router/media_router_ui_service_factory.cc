/* Copyright 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "chrome/browser/ui/media_router/media_router_ui_service_factory.h"

#include "brave/common/pref_names.h"
#include "chrome/browser/media/router/chrome_media_router_factory.h"
#include "chrome/browser/ui/toolbar/toolbar_actions_model_factory.h"
#include "chrome/common/pref_names.h"
#include "components/keyed_service/core/keyed_service.h"
#include "components/prefs/pref_service.h"
#include "components/user_prefs/user_prefs.h"
#include "content/public/browser/browser_context.h"

#define BuildServiceInstanceFor BuildServiceInstanceFor_ChromiumImpl
#include "../../../../../../chrome/browser/ui/media_router/media_router_ui_service_factory.cc"  // NOLINT
#undef BuildServiceInstanceFor

namespace media_router {

KeyedService* MediaRouterUIServiceFactory::BuildServiceInstanceFor(
    BrowserContext* context) const {
  auto* pref_service = user_prefs::UserPrefs::Get(context);
  auto enabled = pref_service->GetBoolean(kBraveMediaRouter);
  pref_service->SetBoolean(::prefs::kEnableMediaRouter, enabled);

  return BuildServiceInstanceFor_ChromiumImpl(context);
}

}  // namespace media_router

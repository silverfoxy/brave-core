/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_CHROMIUM_SRC_CHROME_BROWSER_UI_MEDIA_ROUTER_MEDIA_ROUTER_SERVICE_FACTORY_H_
#define BRAVE_CHROMIUM_SRC_CHROME_BROWSER_UI_MEDIA_ROUTER_MEDIA_ROUTER_SERVICE_FACTORY_H_

#include "components/keyed_service/content/browser_context_keyed_service_factory.h"

#define BuildServiceInstanceFor                                          \
  BuildServiceInstanceFor_ChromiumImpl(content::BrowserContext* profile) \
      const;                                                             \
  KeyedService* BuildServiceInstanceFor
#include "../../../../../../chrome/browser/ui/media_router/media_router_ui_service_factory.h"  // NOLINT
#undef BuildServiceInstanceFor

#endif  // BRAVE_CHROMIUM_SRC_CHROME_BROWSER_UI_MEDIA_ROUTER_MEDIA_ROUTER_SERVICE_FACTORY_H

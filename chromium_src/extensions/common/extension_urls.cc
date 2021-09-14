/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "extensions/common/extension_urls.h"

#include "brave/components/brave_component_updater/browser/brave_component.h"

#define GetDefaultWebstoreUpdateUrl GetDefaultWebstoreUpdateUrl_ChromiumImpl
#include "../../../../extensions/common/extension_urls.cc"
#undef GetDefaultWebstoreUpdateUrl

namespace extension_urls {

GURL GetDefaultWebstoreUpdateUrl() {
  return GURL(brave_component_updater::GetUpdateURLHost());
}

}  // namespace extension_urls

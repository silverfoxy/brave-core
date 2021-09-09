// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#ifndef BRAVE_CHROMIUM_SRC_CHROME_COMMON_EXTENSIONS_CHROME_EXTENSIONS_CLIENT_H_
#define BRAVE_CHROMIUM_SRC_CHROME_COMMON_EXTENSIONS_CHROME_EXTENSIONS_CLIENT_H_

#define ChromeExtensionsClient ChromeExtensionsClient_ChromiumImpl
#include "../../../../../chrome/common/extensions/chrome_extensions_client.h"
#undef ChromeExtensionsClient

namespace extensions {

class ChromeExtensionsClient : public ChromeExtensionsClient_ChromiumImpl {
 public:
  using ChromeExtensionsClient_ChromiumImpl::
      ChromeExtensionsClient_ChromiumImpl;

  void InitializeWebStoreUpdateURL();
  const GURL& GetWebstoreUpdateURL() const override;

 private:
  GURL webstore_update_url_;
  DISALLOW_COPY_AND_ASSIGN(ChromeExtensionsClient);
};

}  // namespace extensions

#endif  // BRAVE_CHROMIUM_SRC_CHROME_COMMON_EXTENSIONS_CHROME_EXTENSIONS_CLIENT_H_

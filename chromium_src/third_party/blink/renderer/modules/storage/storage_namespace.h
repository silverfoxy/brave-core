/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_CHROMIUM_SRC_THIRD_PARTY_BLINK_RENDERER_MODULES_STORAGE_STORAGE_NAMESPACE_H_
#define BRAVE_CHROMIUM_SRC_THIRD_PARTY_BLINK_RENDERER_MODULES_STORAGE_STORAGE_NAMESPACE_H_

namespace blink {
class BlinkStorageKey;
}  // namespace blink

// Add an overload for GetCachedArea that takes BlinkStorageKey
#define GetCachedArea                                                    \
  GetCachedArea(                                                         \
      const BlinkStorageKey& storage_key,                                \
      const LocalDOMWindow* local_dom_window,                            \
      mojo::PendingRemote<mojom::blink::StorageArea> storage_area = {}); \
  scoped_refptr<CachedStorageArea> GetCachedArea

#include "../../../../../../../third_party/blink/renderer/modules/storage/storage_namespace.h"
#undef GetCachedArea

#endif  // BRAVE_CHROMIUM_SRC_THIRD_PARTY_BLINK_RENDERER_MODULES_STORAGE_STORAGE_NAMESPACE_H_

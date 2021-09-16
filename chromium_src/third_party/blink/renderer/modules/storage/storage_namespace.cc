/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "../../../../../../../third_party/blink/renderer/modules/storage/storage_namespace.cc"

namespace blink {

scoped_refptr<CachedStorageArea> StorageNamespace::GetCachedArea(
    const BlinkStorageKey& storage_key,
    const LocalDOMWindow* local_dom_window,
    mojo::PendingRemote<mojom::blink::StorageArea> storage_area) {
  // These values are persisted to logs. Entries should not be renumbered and
  // numeric values should never be reused.
  enum class CacheMetrics {
    kMiss = 0,    // Area not in cache.
    kHit = 1,     // Area with refcount = 0 loaded from cache.
    kUnused = 2,  // Cache was not used. Area had refcount > 0.
    kMaxValue = kUnused,
  };

  CacheMetrics metric = CacheMetrics::kMiss;
  scoped_refptr<CachedStorageArea> result;
  auto cache_it = cached_areas_.find(&storage_key);
  if (cache_it != cached_areas_.end()) {
    metric = cache_it->value->HasOneRef() ? CacheMetrics::kHit
                                          : CacheMetrics::kUnused;
    result = cache_it->value;
  }
  if (IsSessionStorage()) {
    base::UmaHistogramEnumeration("Storage.SessionStorage.RendererAreaCacheHit",
                                  metric);
  } else {
    base::UmaHistogramEnumeration("LocalStorage.RendererAreaCacheHit", metric);
  }

  if (result)
    return result;

  controller_->ClearAreasIfNeeded();
  result = base::MakeRefCounted<CachedStorageArea>(
      IsSessionStorage() ? CachedStorageArea::AreaType::kSessionStorage
                         : CachedStorageArea::AreaType::kLocalStorage,
      storage_key, local_dom_window, controller_->TaskRunner(), this,
      /*is_session_storage_for_prerendering=*/false, std::move(storage_area));
  cached_areas_.insert(std::make_unique<const BlinkStorageKey>(storage_key),
                       result);
  return result;
}

}  // namespace blink

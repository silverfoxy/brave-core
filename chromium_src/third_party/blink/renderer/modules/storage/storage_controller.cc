/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "../../../../../../../third_party/blink/renderer/modules/storage/storage_controller.cc"

namespace blink {

scoped_refptr<CachedStorageArea> StorageController::GetLocalStorageArea(
    const BlinkStorageKey& storage_key,
    const LocalDOMWindow* local_dom_window,
    mojo::PendingRemote<mojom::blink::StorageArea> local_storage_area) {
  DCHECK_CALLED_ON_VALID_SEQUENCE(sequence_checker_);
  EnsureLocalStorageNamespaceCreated();
  return local_storage_namespace_->GetCachedArea(storage_key, local_dom_window,
                                                 std::move(local_storage_area));
}

}  // namespace blink

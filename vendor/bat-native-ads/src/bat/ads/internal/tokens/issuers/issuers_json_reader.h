/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_JSON_READER_H_
#define BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_JSON_READER_H_

#include <string>

#include "bat/ads/internal/tokens/issuers/issuer_info_aliases.h"

namespace absl {
template <typename T>
class optional;
}  // namespace absl

namespace ads {
namespace JSONReader {

absl::optional<IssuerList> ReadIssuers(const std::string& json);

}  // namespace JSONReader
}  // namespace ads

#endif  // BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_TOKENS_ISSUERS_ISSUERS_JSON_READER_H_

/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_json_reader.h"

#include "base/json/json_reader.h"
#include "bat/ads/internal/tokens/issuers/issuers_parser.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace ads {
namespace JSONReader {

absl::optional<IssuerList> ReadIssuers(const std::string& json) {
  const absl::optional<base::Value> value = base::JSONReader::Read(json);
  return ToIssuerListFromValue(value.value());
}

}  // namespace JSONReader
}  // namespace ads

/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_json_writer.h"

#include "base/json/json_writer.h"
#include "base/values.h"
#include "bat/ads/internal/tokens/issuers/issuers_parser.h"

namespace ads {
namespace JSONWriter {

std::string WriteIssuers(const IssuerList& issuers) {
  const base::Value issuer_list = FromIssuerListToValue(issuers);

  std::string json;
  base::JSONWriter::Write(issuer_list, &json);

  return json;
}

}  // namespace JSONWriter
}  // namespace ads

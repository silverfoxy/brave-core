/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_util.h"

#include <algorithm>

#include "bat/ads/internal/account/confirmations/confirmations_state.h"

namespace ads {

namespace {

absl::optional<IssuerInfo> GetIssuerForType(const IssuerType issuer_type) {
  const IssuerList issuers = ConfirmationsState::Get()->GetIssuers();

  const auto iter = std::find_if(issuers.begin(), issuers.end(),
                                 [&issuer_type](const IssuerInfo& issuer) {
                                   return issuer.type == issuer_type;
                                 });

  if (iter == issuers.end()) {
    return absl::nullopt;
  }

  return *iter;
}

bool PublicKeyExists(const IssuerInfo& issuer, const std::string& public_key) {
  const auto iter = std::find(issuer.public_keys.begin(),
                              issuer.public_keys.end(), public_key);

  if (iter == issuer.public_keys.end()) {
    return false;
  }

  return true;
}

}  // namespace

bool HasIssuersChanged(const IssuerList& issuers) {
  const IssuerList last_issuers = ConfirmationsState::Get()->GetIssuers();
  if (issuers == last_issuers) {
    return false;
  }

  return true;
}

bool IssuerExistsForType(const IssuerType issuer_type) {
  const absl::optional<IssuerInfo> issuer = GetIssuerForType(issuer_type);
  if (!issuer) {
    return false;
  }

  return true;
}

bool PublicKeyExistsForType(const IssuerType issuer_type,
                            const std::string& public_key) {
  const absl::optional<IssuerInfo> issuer = GetIssuerForType(issuer_type);
  if (!issuer) {
    return false;
  }

  return PublicKeyExists(issuer.value(), public_key);
}

}  // namespace ads

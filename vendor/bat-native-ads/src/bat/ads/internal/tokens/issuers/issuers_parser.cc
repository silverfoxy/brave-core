/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_parser.h"

#include <string>
#include <utility>
#include <vector>

#include "base/values.h"
#include "bat/ads/internal/tokens/issuers/issuer_types.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace ads {

namespace {

constexpr char kNameKey[] = "name";
constexpr char kPublicKeysKey[] = "publicKeys";

constexpr char kConfirmationName[] = "confirmations";
constexpr char kPaymentsName[] = "payments";

std::string GetNameForIssuerType(const IssuerType type) {
  switch (type) {
    case IssuerType::kConfirmation: {
      return kConfirmationName;
    }

    case IssuerType::kPayments: {
      return kPaymentsName;
    }
  }
}

absl::optional<IssuerType> ParseIssuerType(const base::Value& value) {
  const std::string* const name_value = value.FindStringKey(kNameKey);
  if (!name_value) {
    return absl::nullopt;
  }

  if (*name_value == kConfirmationName) {
    return IssuerType::kConfirmation;
  } else if (*name_value == kPaymentsName) {
    return IssuerType::kPayments;
  }

  return absl::nullopt;
}

absl::optional<std::vector<std::string>> ParsePublicKeys(
    const base::Value& value) {
  const base::Value* const public_keys_value =
      value.FindListKey(kPublicKeysKey);
  if (!public_keys_value) {
    return absl::nullopt;
  }

  std::vector<std::string> public_keys;
  for (const auto& public_key_value : public_keys_value->GetList()) {
    if (!public_key_value.is_string()) {
      return absl::nullopt;
    }

    public_keys.push_back(public_key_value.GetString());
  }

  return public_keys;
}

}  // namespace

base::Value FromIssuerListToValue(const IssuerList& issuers) {
  base::Value issuers_list(base::Value::Type::LIST);

  for (const auto& issuer : issuers) {
    base::Value issuer_dictionary(base::Value::Type::DICTIONARY);

    const std::string name = GetNameForIssuerType(issuer.type);
    issuer_dictionary.SetKey(kNameKey, base::Value(name));

    base::Value public_keys_list(base::Value::Type::LIST);
    for (const auto& public_key : issuer.public_keys) {
      public_keys_list.Append(public_key);
    }
    issuer_dictionary.SetKey(kPublicKeysKey, std::move(public_keys_list));

    issuers_list.Append(std::move(issuer_dictionary));
  }

  return issuers_list;
}

absl::optional<IssuerList> ToIssuerListFromValue(const base::Value& value) {
  if (!value.is_list()) {
    return absl::nullopt;
  }

  IssuerList issuers;

  for (const auto& issuer_value : value.GetList()) {
    if (!issuer_value.is_dict()) {
      return absl::nullopt;
    }

    absl::optional<IssuerType> type = ParseIssuerType(issuer_value);
    if (!type) {
      return absl::nullopt;
    }

    absl::optional<std::vector<std::string>> public_keys =
        ParsePublicKeys(issuer_value);
    if (!public_keys) {
      return absl::nullopt;
    }

    IssuerInfo issuer;
    issuer.type = type.value();
    issuer.public_keys = public_keys.value();

    issuers.push_back(issuer);
  }

  return issuers;
}

}  // namespace ads

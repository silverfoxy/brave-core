/* Copyright (c) 2020 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_ACCOUNT_CONFIRMATIONS_CONFIRMATIONS_H_
#define BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_ACCOUNT_CONFIRMATIONS_CONFIRMATIONS_H_

#include <memory>
#include <string>

#include "base/observer_list.h"
#include "bat/ads/internal/account/confirmations/confirmations_observer.h"
#include "bat/ads/internal/timer.h"
#include "bat/ads/internal/tokens/redeem_unblinded_token/redeem_unblinded_token_delegate.h"

namespace base {
class DictionaryValue;
}  // namespace base

namespace ads {

class AdRewards;
class ConfirmationType;
class ConfirmationsState;
class RedeemUnblindedToken;
struct CatalogIssuersInfo;

namespace privacy {
class TokenGeneratorInterface;
}  // namespace privacy

class Confirmations : public RedeemUnblindedTokenDelegate {
 public:
  Confirmations(privacy::TokenGeneratorInterface* token_generator,
                AdRewards* ad_rewards);

  ~Confirmations() override;

  void AddObserver(ConfirmationsObserver* observer);
  void RemoveObserver(ConfirmationsObserver* observer);

  void SetCatalogIssuers(const CatalogIssuersInfo& catalog_issuers);

  void Confirm(const std::string& creative_instance_id,
               const ConfirmationType& confirmation_type);

  void RetryAfterDelay();

 private:
  base::ObserverList<ConfirmationsObserver> observers_;

  privacy::TokenGeneratorInterface* token_generator_;  // NOT OWNED

  std::unique_ptr<ConfirmationsState> confirmations_state_;
  std::unique_ptr<RedeemUnblindedToken> redeem_unblinded_token_;

  ConfirmationInfo CreateConfirmation(
      const std::string& creative_instance_id,
      const ConfirmationType& confirmation_type,
      const base::DictionaryValue& user_data) const;

  Timer retry_timer_;
  void CreateNewConfirmationAndAppendToRetryQueue(
      const ConfirmationInfo& confirmation);
  void AppendToRetryQueue(const ConfirmationInfo& confirmation);
  void RemoveFromRetryQueue(const ConfirmationInfo& confirmation);
  void Retry();

  void NotifyDidConfirm(const double estimated_redemption_value,
                        const ConfirmationInfo& confirmation) const;

  void NotifyFailedToConfirm(const ConfirmationInfo& confirmation) const;

  // RedeemUnblindedTokenDelegate:
  void OnDidSendConfirmation(const ConfirmationInfo& confirmation) override;

  void OnDidRedeemUnblindedToken(
      const ConfirmationInfo& confirmation,
      const privacy::UnblindedTokenInfo& unblinded_payment_token) override;

  void OnFailedToRedeemUnblindedToken(const ConfirmationInfo& confirmation,
                                      const bool should_retry) override;
};

}  // namespace ads

#endif  // BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_ACCOUNT_CONFIRMATIONS_CONFIRMATIONS_H_

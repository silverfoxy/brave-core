/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers.h"

#include <cstdint>
#include <utility>

#include "base/bind.h"
#include "base/check_op.h"
#include "base/time/time.h"
#include "bat/ads/ads_client.h"
#include "bat/ads/internal/ads_client_helper.h"
#include "bat/ads/internal/logging.h"
#include "bat/ads/internal/logging_util.h"
#include "bat/ads/internal/time_formatting_util.h"
#include "bat/ads/internal/tokens/issuers/issuers_delegate.h"
#include "bat/ads/internal/tokens/issuers/issuers_json_reader.h"
#include "bat/ads/internal/tokens/issuers/issuers_url_request_builder.h"
#include "net/http/http_status_code.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace ads {

namespace {

const int64_t kFetchAfterSeconds = 1 * base::Time::kSecondsPerHour;

const int64_t kRetryAfterSeconds = 1 * base::Time::kSecondsPerMinute;

}  // namespace

Issuers::Issuers() = default;

Issuers::~Issuers() {
  delegate_ = nullptr;
}

void Issuers::set_delegate(IssuersDelegate* delegate) {
  DCHECK_EQ(delegate_, nullptr);
  delegate_ = delegate;
}

void Issuers::MaybeFetch() {
  if (is_processing_ || retry_timer_.IsRunning()) {
    return;
  }

  Fetch();
}

//////////////////////////////////////////////////////////////////////////////

void Issuers::Fetch() {
  DCHECK(!is_processing_);

  BLOG(1, "GetIssuers");
  BLOG(2, "GET /v1/issuers/");

  IssuersUrlRequestBuilder url_request_builder;
  mojom::UrlRequestPtr url_request = url_request_builder.Build();
  BLOG(6, UrlRequestToString(url_request));
  BLOG(7, UrlRequestHeadersToString(url_request));

  const auto callback =
      std::bind(&Issuers::OnFetch, this, std::placeholders::_1);
  AdsClientHelper::Get()->UrlRequest(std::move(url_request), callback);
}

void Issuers::OnFetch(const mojom::UrlResponse& url_response) {
  BLOG(1, "OnGetIssuers");

  BLOG(6, UrlResponseToString(url_response));
  BLOG(7, UrlResponseHeadersToString(url_response));

  if (url_response.status_code != net::HTTP_OK) {
    OnFailedToGetIssuers();
    return;
  }

  const absl::optional<IssuerList> issuers =
      JSONReader::ReadIssuers(url_response.body);
  if (!issuers) {
    BLOG(3, "Failed to parse response: " << url_response.body);
    OnFailedToGetIssuers();
    return;
  }

  OnDidGetIssuers(issuers.value());
}

void Issuers::OnDidGetIssuers(const IssuerList& issuers) {
  is_processing_ = false;

  if (delegate_) {
    delegate_->OnDidGetIssuers(issuers);
  }

  FetchAfterDelay();
}

void Issuers::OnFailedToGetIssuers() {
  is_processing_ = false;

  if (delegate_) {
    delegate_->OnFailedToGetIssuers();
  }

  Retry();
}

void Issuers::FetchAfterDelay() {
  retry_timer_.Stop();

  const base::TimeDelta delay =
      base::TimeDelta::FromSeconds(kFetchAfterSeconds);

  const base::Time time = timer_.StartWithPrivacy(
      delay, base::BindOnce(&Issuers::Fetch, base::Unretained(this)));

  BLOG(1, "Fetch issuers " << FriendlyDateAndTime(time));
}

void Issuers::Retry() {
  const base::Time time = retry_timer_.StartWithPrivacy(
      base::TimeDelta::FromSeconds(kRetryAfterSeconds),
      base::BindOnce(&Issuers::OnRetry, base::Unretained(this)));

  BLOG(1, "Retry fetching issuers " << FriendlyDateAndTime(time));
}

void Issuers::OnRetry() {
  BLOG(1, "Retry fetching issuers");

  Fetch();
}

}  // namespace ads

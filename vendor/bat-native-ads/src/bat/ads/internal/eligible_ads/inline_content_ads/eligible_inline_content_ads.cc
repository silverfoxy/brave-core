/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/eligible_ads/inline_content_ads/eligible_inline_content_ads.h"

#include "base/check.h"
#include "bat/ads/inline_content_ad_info.h"
#include "bat/ads/internal/ad_pacing/ad_pacing.h"
#include "bat/ads/internal/ad_priority/ad_priority.h"
#include "bat/ads/internal/ad_serving/ad_targeting/geographic/subdivision/subdivision_targeting.h"
#include "bat/ads/internal/ad_targeting/ad_targeting.h"
#include "bat/ads/internal/ad_targeting/ad_targeting_user_model_info.h"
#include "bat/ads/internal/ads/inline_content_ads/inline_content_ad_exclusion_rules.h"
#include "bat/ads/internal/ads_client_helper.h"
#include "bat/ads/internal/database/tables/ad_events_database_table.h"
#include "bat/ads/internal/database/tables/creative_inline_content_ads_database_table.h"
#include "bat/ads/internal/eligible_ads/eligible_ads_constants.h"
#include "bat/ads/internal/eligible_ads/eligible_ads_predictor_util.h"
#include "bat/ads/internal/eligible_ads/eligible_ads_util.h"
#include "bat/ads/internal/eligible_ads/sample_ads.h"
#include "bat/ads/internal/eligible_ads/seen_ads.h"
#include "bat/ads/internal/eligible_ads/seen_advertisers.h"
#include "bat/ads/internal/features/ad_serving/ad_serving_features.h"
#include "bat/ads/internal/logging.h"
#include "bat/ads/internal/resources/frequency_capping/anti_targeting_resource.h"
#include "bat/ads/internal/segments/segments_util.h"

namespace ads {
namespace inline_content_ads {

namespace {

bool ShouldCapLastServedAd(const CreativeInlineContentAdList& ads) {
  return ads.size() != 1;
}

}  // namespace

EligibleAds::EligibleAds(
    ad_targeting::geographic::SubdivisionTargeting* subdivision_targeting,
    resource::AntiTargeting* anti_targeting)
    : subdivision_targeting_(subdivision_targeting),
      anti_targeting_resource_(anti_targeting) {
  DCHECK(subdivision_targeting_);
  DCHECK(anti_targeting_resource_);
}

EligibleAds::~EligibleAds() = default;

void EligibleAds::SetLastServedAd(const CreativeAdInfo& creative_ad) {
  last_served_creative_ad_ = creative_ad;
}

void EligibleAds::Get(const ad_targeting::UserModelInfo& user_model,
                      const std::string& dimensions,
                      GetCallback callback) {
  database::table::AdEvents database_table;
  database_table.GetAll([=](const bool success, const AdEventList& ad_events) {
    if (!success) {
      BLOG(1, "Failed to get ad events");
      callback(/* was_allowed */ false, {});
      return;
    }

    const int max_count = features::GetBrowsingHistoryMaxCount();
    const int days_ago = features::GetBrowsingHistoryDaysAgo();
    AdsClientHelper::Get()->GetBrowsingHistory(
        max_count, days_ago, [=](const BrowsingHistoryList& browsing_history) {
          GetForParentChildSegments(user_model, dimensions, ad_events,
                                    browsing_history, callback);
        });
  });
}

void EligibleAds::GetV2(const ad_targeting::UserModelInfo& user_model,
                        const std::string& dimensions,
                        GetV2Callback callback) {
  database::table::AdEvents database_table;
  database_table.GetAll([=](const bool success, const AdEventList& ad_events) {
    if (!success) {
      BLOG(1, "Failed to get ad events");
      callback(/* was_allowed */ false, absl::nullopt);
      return;
    }

    const int max_count = features::GetBrowsingHistoryMaxCount();
    const int days_ago = features::GetBrowsingHistoryDaysAgo();
    AdsClientHelper::Get()->GetBrowsingHistory(
        max_count, days_ago, [=](const BrowsingHistoryList& history) {
          GetEligibleAds(user_model, ad_events, history, dimensions, callback);
        });
  });
}

///////////////////////////////////////////////////////////////////////////////

void EligibleAds::GetEligibleAds(const ad_targeting::UserModelInfo& user_model,
                                 const AdEventList& ad_events,
                                 const BrowsingHistoryList& browsing_history,
                                 const std::string& dimensions,
                                 GetV2Callback callback) const {
  BLOG(1, "Get eligible ads");

  database::table::CreativeInlineContentAds database_table;
  database_table.GetForDimensions(
      dimensions,
      [=](const bool success, const CreativeInlineContentAdList& ads) {
        if (!success) {
          BLOG(1, "Failed to get inline content ads");
          callback(/* was_allowed */ false, absl::nullopt);
          return;
        }

        if (ads.empty()) {
          BLOG(1, "No inline content ads");
          callback(/* was_allowed */ true, absl::nullopt);
          return;
        }

        CreativeInlineContentAdList eligible_ads = ApplyFrequencyCapping(
            ads,
            ShouldCapLastServedAd(ads) ? last_served_creative_ad_
                                       : CreativeAdInfo(),
            ad_events, browsing_history);

        if (eligible_ads.empty()) {
          BLOG(1, "No eligible ads");
          callback(/* was_allowed */ true, absl::nullopt);
          return;
        }

        ChooseAd(user_model, ad_events, eligible_ads, callback);
      });
}

void EligibleAds::ChooseAd(const ad_targeting::UserModelInfo& user_model,
                           const AdEventList& ad_events,
                           const CreativeInlineContentAdList& eligible_ads,
                           GetV2Callback callback) const {
  DCHECK(!eligible_ads.empty());

  const CreativeInlineContentAdPredictorMap ads =
      GroupEligibleAdsByCreativeInstanceId(eligible_ads);

  const CreativeInlineContentAdPredictorMap ads_with_features =
      ComputePredictorFeaturesAndScores(ads, user_model, ad_events);

  const absl::optional<CreativeInlineContentAdInfo> ad =
      SampleFromAds(ads_with_features);

  callback(/* was_allowed */ true, ad);
}

void EligibleAds::GetForParentChildSegments(
    const ad_targeting::UserModelInfo& user_model,
    const std::string& dimensions,
    const AdEventList& ad_events,
    const BrowsingHistoryList& browsing_history,
    GetCallback callback) const {
  const SegmentList segments =
      ad_targeting::GetTopParentChildSegments(user_model);
  if (segments.empty()) {
    GetForParentSegments(user_model, dimensions, ad_events, browsing_history,
                         callback);
    return;
  }

  BLOG(1, "Get eligible ads for parent-child segments:");
  for (const auto& segment : segments) {
    BLOG(1, "  " << segment);
  }

  database::table::CreativeInlineContentAds database_table;
  database_table.GetForSegmentsAndDimensions(
      segments, dimensions,
      [=](const bool success, const SegmentList& segments,
          const CreativeInlineContentAdList& ads) {
        CreativeInlineContentAdList eligible_ads =
            FilterIneligibleAds(ads, ad_events, browsing_history);

        if (eligible_ads.empty()) {
          BLOG(1, "No eligible ads for parent-child segments");
          GetForParentSegments(user_model, dimensions, ad_events,
                               browsing_history, callback);
          return;
        }

        callback(/* was_allowed */ true, eligible_ads);
      });
}

void EligibleAds::GetForParentSegments(
    const ad_targeting::UserModelInfo& user_model,
    const std::string& dimensions,
    const AdEventList& ad_events,
    const BrowsingHistoryList& browsing_history,
    GetCallback callback) const {
  const SegmentList segments = ad_targeting::GetTopParentSegments(user_model);
  if (segments.empty()) {
    GetForUntargeted(dimensions, ad_events, browsing_history, callback);
    return;
  }

  BLOG(1, "Get eligible ads for parent segments:");
  for (const auto& segment : segments) {
    BLOG(1, "  " << segment);
  }

  database::table::CreativeInlineContentAds database_table;
  database_table.GetForSegmentsAndDimensions(
      segments, dimensions,
      [=](const bool success, const SegmentList& segments,
          const CreativeInlineContentAdList& ads) {
        CreativeInlineContentAdList eligible_ads =
            FilterIneligibleAds(ads, ad_events, browsing_history);

        if (eligible_ads.empty()) {
          BLOG(1, "No eligible ads for parent segments");
          GetForUntargeted(dimensions, ad_events, browsing_history, callback);
          return;
        }

        callback(/* was_allowed */ true, eligible_ads);
      });
}

void EligibleAds::GetForUntargeted(const std::string& dimensions,
                                   const AdEventList& ad_events,
                                   const BrowsingHistoryList& browsing_history,
                                   GetCallback callback) const {
  BLOG(1, "Get eligible ads for untargeted segment");

  database::table::CreativeInlineContentAds database_table;
  database_table.GetForSegmentsAndDimensions(
      {kUntargeted}, dimensions,
      [=](const bool success, const SegmentList& segments,
          const CreativeInlineContentAdList& ads) {
        CreativeInlineContentAdList eligible_ads =
            FilterIneligibleAds(ads, ad_events, browsing_history);

        if (eligible_ads.empty()) {
          BLOG(1, "No eligible ads for untargeted segment");
        }

        callback(/* was_allowed */ true, eligible_ads);
      });
}

CreativeInlineContentAdList EligibleAds::FilterIneligibleAds(
    const CreativeInlineContentAdList& ads,
    const AdEventList& ad_events,
    const BrowsingHistoryList& browsing_history) const {
  if (ads.empty()) {
    return {};
  }

  CreativeInlineContentAdList eligible_ads = ads;

  eligible_ads = FilterSeenAdvertisersAndRoundRobinIfNeeded(
      eligible_ads, AdType::kInlineContentAd);

  eligible_ads = FilterSeenAdsAndRoundRobinIfNeeded(eligible_ads,
                                                    AdType::kInlineContentAd);

  eligible_ads = ApplyFrequencyCapping(
      eligible_ads,
      ShouldCapLastServedAd(ads) ? last_served_creative_ad_ : CreativeAdInfo(),
      ad_events, browsing_history);

  eligible_ads = PaceAds(eligible_ads);

  eligible_ads = PrioritizeAds(eligible_ads);

  return eligible_ads;
}

CreativeInlineContentAdList EligibleAds::ApplyFrequencyCapping(
    const CreativeInlineContentAdList& ads,
    const CreativeAdInfo& last_served_creative_ad,
    const AdEventList& ad_events,
    const BrowsingHistoryList& browsing_history) const {
  CreativeInlineContentAdList eligible_ads = ads;

  const frequency_capping::ExclusionRules exclusion_rules(
      subdivision_targeting_, anti_targeting_resource_, ad_events,
      browsing_history);

  const auto iter = std::remove_if(
      eligible_ads.begin(), eligible_ads.end(),
      [&exclusion_rules, &last_served_creative_ad](CreativeAdInfo& ad) {
        return exclusion_rules.ShouldExcludeAd(ad) ||
               ad.creative_instance_id ==
                   last_served_creative_ad.creative_instance_id;
      });

  eligible_ads.erase(iter, eligible_ads.end());

  return eligible_ads;
}

}  // namespace inline_content_ads
}  // namespace ads

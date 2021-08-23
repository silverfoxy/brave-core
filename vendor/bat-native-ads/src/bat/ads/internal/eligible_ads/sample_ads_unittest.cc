/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/eligible_ads/sample_ads.h"

#include "base/guid.h"
#include "bat/ads/internal/bundle/creative_ad_notification_info.h"
#include "bat/ads/internal/bundle/creative_ad_notification_unittest_util.h"
#include "bat/ads/internal/eligible_ads/eligible_ads_util.h"
#include "bat/ads/internal/unittest_util.h"

// npm run test -- brave_unit_tests --filter=BatAds*

namespace ads {

TEST(BatAdsSampleAdsTest, CalculateNormalisingConstantWithEmptyAds) {
  // Arrange
  CreativeAdNotificationPredictorMap ads;

  // Act
  const double normalising_constant = CalculateNormalisingConstant(ads);

  // Assert
  const double expected_normalising_constant = 0.0;
  EXPECT_TRUE(
      DoubleEquals(expected_normalising_constant, normalising_constant));
}

TEST(BatAdsSampleAdsTest, CalculateNormalisingConstant) {
  // Arrange
  CreativeAdNotificationPredictorMap ads;
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_1;
  ad_predictor_1.score = 1.1;
  ads[base::GenerateGUID()] = ad_predictor_1;

  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_2;
  ad_predictor_2.score = 2.2;
  ads[base::GenerateGUID()] = ad_predictor_2;

  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_3;
  ad_predictor_3.score = 3.3;
  ads[base::GenerateGUID()] = ad_predictor_3;

  // Act
  const double normalising_constant = CalculateNormalisingConstant(ads);

  // Assert
  const double expected_normalising_constant = 6.6;
  EXPECT_TRUE(
      DoubleEquals(expected_normalising_constant, normalising_constant));
}

TEST(BatAdsSampleAdsTest, SampleFromAdsWithZeroScores) {
  // Arrange
  CreativeAdNotificationPredictorMap ads;
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_1;
  ad_predictor_1.score = 0;
  ads[base::GenerateGUID()] = ad_predictor_1;

  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_2;
  ad_predictor_2.score = 0;
  ads[base::GenerateGUID()] = ad_predictor_2;

  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_3;
  ad_predictor_3.score = 0;
  ads[base::GenerateGUID()] = ad_predictor_3;

  // Act
  const absl::optional<CreativeAdNotificationInfo> ad = SampleFromAds(ads);

  // Assert
  const absl::optional<CreativeAdNotificationInfo> expected_ad = absl::nullopt;
  EXPECT_EQ(expected_ad, ad);
}

TEST(BatAdsSampleAdsTest, DeterministicallySampleFromAdsWithOneNonZeroScore) {
  // Arrange
  CreativeAdNotificationPredictorMap ads;

  CreativeAdNotificationInfo creative_ad_notification_1 =
      GetCreativeAdNotification("foo-bar", 1.0, 1);
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_1;
  ad_predictor_1.score = 0;
  ad_predictor_1.creative_ad = creative_ad_notification_1;
  ads[creative_ad_notification_1.creative_instance_id] = ad_predictor_1;

  CreativeAdNotificationInfo creative_ad_notification_2 =
      GetCreativeAdNotification("foo-bar", 1.0, 1);
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_2;
  ad_predictor_2.score = 0.1;
  ad_predictor_2.creative_ad = creative_ad_notification_2;
  ads[creative_ad_notification_2.creative_instance_id] = ad_predictor_2;

  CreativeAdNotificationInfo creative_ad_notification_3 =
      GetCreativeAdNotification("foo-bar", 1.0, 1);
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_3;
  ad_predictor_3.score = 0;
  ad_predictor_3.creative_ad = creative_ad_notification_3;
  ads[creative_ad_notification_3.creative_instance_id] = ad_predictor_3;

  // Act
  for (int i = 0; i < 10; i++) {
    CreativeAdNotificationInfo expected_ad = creative_ad_notification_2;
    const absl::optional<CreativeAdNotificationInfo> ad = SampleFromAds(ads);
    EXPECT_EQ(expected_ad, ad.value());
  }

  // Assert
}

TEST(BatAdsSampleAdsTest, ProbabilisticallySampleFromAds) {
  // Arrange
  CreativeAdNotificationPredictorMap ads;

  CreativeAdNotificationInfo creative_ad_notification_1 =
      GetCreativeAdNotification("foo-bar", 1.0, 1);
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_1;
  ad_predictor_1.creative_ad = creative_ad_notification_1;
  ad_predictor_1.score = 3;
  ads[creative_ad_notification_1.creative_instance_id] = ad_predictor_1;

  CreativeAdNotificationInfo creative_ad_notification_2 =
      GetCreativeAdNotification("foo-bar", 1.0, 1);
  AdPredictorInfo<CreativeAdNotificationInfo> ad_predictor_2;
  ad_predictor_2.creative_ad = creative_ad_notification_2;
  ad_predictor_2.score = 3;
  ads[creative_ad_notification_2.creative_instance_id] = ad_predictor_2;

  // Act
  int ads_1_count = 0;
  int ads_2_count = 0;

  // P(X>1) > 0.99999999 with X~Bin(n=25, p=0.5), i.e. less than 1 in 100M tests
  // are expected to fail
  for (int i = 0; i < 25; i++) {
    const absl::optional<CreativeAdNotificationInfo> ad = SampleFromAds(ads);

    if (ad.value().creative_instance_id ==
        creative_ad_notification_1.creative_instance_id) {
      ads_1_count += 1;
    } else if (ad.value().creative_instance_id ==
               creative_ad_notification_2.creative_instance_id) {
      ads_2_count += 1;
    }
  }

  // Assert
  EXPECT_FALSE(ads_1_count == 0 || ads_2_count == 0);
}

}  // namespace ads

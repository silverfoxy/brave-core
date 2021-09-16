/* Copyright (c) 2020 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_DATABASE_TABLES_CREATIVE_NEW_TAB_PAGE_ADS_DATABASE_TABLE_H_
#define BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_DATABASE_TABLES_CREATIVE_NEW_TAB_PAGE_ADS_DATABASE_TABLE_H_

#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "bat/ads/ads_client_aliases.h"
#include "bat/ads/internal/bundle/creative_new_tab_page_ad_info_aliases.h"
#include "bat/ads/internal/database/database_table.h"
#include "bat/ads/internal/segments/segments_aliases.h"
#include "bat/ads/public/interfaces/ads.mojom.h"

namespace ads {

using GetCreativeNewTabPageAdCallback =
    std::function<void(const bool,
                       const std::string& creative_instance_id,
                       const CreativeNewTabPageAdInfo&)>;

using GetCreativeNewTabPageAdsCallback =
    std::function<void(const bool,
                       const std::vector<std::string>&,
                       const CreativeNewTabPageAdList&)>;

struct CreativeNewTabPageAdInfo;

namespace database {
namespace table {

class Campaigns;
class CreativeAds;
class Dayparts;
class GeoTargets;
class Segments;

class CreativeNewTabPageAds final : public Table {
 public:
  CreativeNewTabPageAds();

  ~CreativeNewTabPageAds() override;

  void Save(const CreativeNewTabPageAdList& creative_new_tab_page_ads,
            ResultCallback callback);

  void Delete(ResultCallback callback);

  void GetForCreativeInstanceId(const std::string& creative_instance_id,
                                GetCreativeNewTabPageAdCallback callback);

  void GetForSegments(const SegmentList& segments,
                      GetCreativeNewTabPageAdsCallback callback);

  void GetAll(GetCreativeNewTabPageAdsCallback callback);

  void set_batch_size(const int batch_size);

  std::string get_table_name() const override;

  void Migrate(mojom::DBTransaction* transaction,
               const int to_version) override;

 private:
  void InsertOrUpdate(
      mojom::DBTransaction* transaction,
      const CreativeNewTabPageAdList& creative_new_tab_page_ads);

  int BindParameters(mojom::DBCommand* command,
                     const CreativeNewTabPageAdList& creative_new_tab_page_ads);

  std::string BuildInsertOrUpdateQuery(
      mojom::DBCommand* command,
      const CreativeNewTabPageAdList& creative_new_tab_page_ads);

  void OnGetForCreativeInstanceId(mojom::DBCommandResponsePtr response,
                                  const std::string& creative_instance_id,
                                  GetCreativeNewTabPageAdCallback callback);

  void OnGetForSegments(mojom::DBCommandResponsePtr response,
                        const SegmentList& segments,
                        GetCreativeNewTabPageAdsCallback callback);

  void OnGetAll(mojom::DBCommandResponsePtr response,
                GetCreativeNewTabPageAdsCallback callback);

  CreativeNewTabPageAdInfo GetFromRecord(mojom::DBRecord* record) const;

  void CreateTableV16(mojom::DBTransaction* transaction);
  void MigrateToV16(mojom::DBTransaction* transaction);

  int batch_size_;

  std::unique_ptr<Campaigns> campaigns_database_table_;
  std::unique_ptr<CreativeAds> creative_ads_database_table_;
  std::unique_ptr<Dayparts> dayparts_database_table_;
  std::unique_ptr<GeoTargets> geo_targets_database_table_;
  std::unique_ptr<Segments> segments_database_table_;
};

}  // namespace table
}  // namespace database
}  // namespace ads

#endif  // BRAVE_VENDOR_BAT_NATIVE_ADS_SRC_BAT_ADS_INTERNAL_DATABASE_TABLES_CREATIVE_NEW_TAB_PAGE_ADS_DATABASE_TABLE_H_

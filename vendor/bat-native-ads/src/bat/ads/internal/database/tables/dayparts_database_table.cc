/* Copyright (c) 2020 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/database/tables/dayparts_database_table.h"

#include <algorithm>
#include <functional>
#include <utility>

#include "base/check.h"
#include "base/strings/stringprintf.h"
#include "bat/ads/ads_client.h"
#include "bat/ads/internal/ads_client_helper.h"
#include "bat/ads/internal/database/database_statement_util.h"
#include "bat/ads/internal/database/database_table_util.h"
#include "bat/ads/internal/database/database_util.h"
#include "bat/ads/internal/frequency_capping/exclusion_rules/daypart_frequency_cap.h"

namespace ads {
namespace database {
namespace table {

namespace {
const char kTableName[] = "dayparts";
}  // namespace

Dayparts::Dayparts() = default;

Dayparts::~Dayparts() = default;

void Dayparts::InsertOrUpdate(mojom::DBTransaction* transaction,
                              const CreativeAdList& creative_ads) {
  DCHECK(transaction);

  if (creative_ads.empty()) {
    return;
  }

  mojom::DBCommandPtr command = mojom::DBCommand::New();
  command->type = mojom::DBCommand::Type::RUN;
  command->command = BuildInsertOrUpdateQuery(command.get(), creative_ads);

  transaction->commands.push_back(std::move(command));
}

void Dayparts::Delete(ResultCallback callback) {
  mojom::DBTransactionPtr transaction = mojom::DBTransaction::New();

  util::Delete(transaction.get(), GetTableName());

  AdsClientHelper::Get()->RunDBTransaction(
      std::move(transaction),
      std::bind(&OnResultCallback, std::placeholders::_1, callback));
}

std::string Dayparts::GetTableName() const {
  return kTableName;
}

void Dayparts::Migrate(mojom::DBTransaction* transaction,
                       const int to_version) {
  DCHECK(transaction);

  switch (to_version) {
    case 16: {
      MigrateToV16(transaction);
      break;
    }

    default: {
      break;
    }
  }
}

///////////////////////////////////////////////////////////////////////////////

int Dayparts::BindParameters(mojom::DBCommand* command,
                             const CreativeAdList& creative_ads) {
  DCHECK(command);

  int count = 0;
  int index = 0;

  for (const auto& creative_ad : creative_ads) {
    for (const auto& daypart : creative_ad.dayparts) {
      BindString(command, index++, creative_ad.campaign_id);
      BindString(command, index++, daypart.dow);
      BindInt(command, index++, daypart.start_minute);
      BindInt(command, index++, daypart.end_minute);

      count++;
    }
  }

  return count;
}

std::string Dayparts::BuildInsertOrUpdateQuery(
    mojom::DBCommand* command,
    const CreativeAdList& creative_ads) {
  const int count = BindParameters(command, creative_ads);

  return base::StringPrintf(
      "INSERT OR REPLACE INTO %s "
      "(campaign_id, "
      "dow, "
      "start_minute, "
      "end_minute) VALUES %s",
      GetTableName().c_str(),
      BuildBindingParameterPlaceholders(4, count).c_str());
}

void Dayparts::CreateTableV16(mojom::DBTransaction* transaction) {
  DCHECK(transaction);

  const std::string query = base::StringPrintf(
      "CREATE TABLE %s "
      "(campaign_id TEXT NOT NULL, "
      "dow TEXT NOT NULL, "
      "start_minute INT NOT NULL, "
      "end_minute INT NOT NULL, "
      "PRIMARY KEY (campaign_id, dow, start_minute, end_minute), "
      "UNIQUE(campaign_id, dow, start_minute, end_minute) "
      "ON CONFLICT REPLACE)",
      GetTableName().c_str());

  mojom::DBCommandPtr command = mojom::DBCommand::New();
  command->type = mojom::DBCommand::Type::EXECUTE;
  command->command = query;

  transaction->commands.push_back(std::move(command));
}

void Dayparts::MigrateToV16(mojom::DBTransaction* transaction) {
  DCHECK(transaction);

  util::Drop(transaction, GetTableName());

  CreateTableV16(transaction);
}

}  // namespace table
}  // namespace database
}  // namespace ads

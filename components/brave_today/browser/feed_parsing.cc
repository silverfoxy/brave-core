// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/components/brave_today/browser/feed_parsing.h"

#include <algorithm>
#include <cstdint>
#include <iterator>
#include <list>
#include <utility>
#include <vector>

#include "base/json/json_reader.h"
#include "base/logging.h"
#include "base/strings/string_number_conversions.h"
#include "base/strings/stringprintf.h"
#include "base/time/time.h"
#include "brave/components/brave_today/common/brave_news.mojom-forward.h"
#include "brave/components/brave_today/common/brave_news.mojom.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace brave_news {

bool ParsePublisherList(const std::string& json,
                        Publishers* publishers) {
  base::JSONReader::ValueWithError value_with_error =
      base::JSONReader::ReadAndReturnValueWithError(
          json, base::JSONParserOptions::JSON_PARSE_RFC);
  absl::optional<base::Value>& records_v = value_with_error.value;
  if (!records_v) {
    LOG(ERROR) << "Invalid response, could not parse JSON, JSON is: " << json;
    return false;
  }
  const base::ListValue* response_list;
  if (!records_v->GetAsList(&response_list)) {
    return false;
  }
  for (const base::Value& publisher_raw : response_list->GetList()) {
    auto publisher = brave_news::mojom::Publisher::New();
    publisher->publisher_id = *publisher_raw.FindStringKey("publisher_id");
    publisher->publisher_name = *publisher_raw.FindStringKey("publisher_name");
    publisher->category_name = *publisher_raw.FindStringKey("category");
    publisher->is_enabled = publisher_raw.FindBoolKey("enabled").value_or(true);
    // (*publishers)[publisher->publisher_id] = std::move(publisher);
    publishers->insert_or_assign(publisher->publisher_id, std::move(publisher));
  }
  return true;
}

bool ParseFeed(const std::string& json,
                        Publishers* publishers,
                        mojom::Feed* feed) {
  base::JSONReader::ValueWithError value_with_error =
      base::JSONReader::ReadAndReturnValueWithError(
          json, base::JSONParserOptions::JSON_PARSE_RFC);
  absl::optional<base::Value>& records_v = value_with_error.value;
  if (!records_v) {
    LOG(ERROR) << "Invalid response, could not parse JSON, JSON is: " << json;
    return false;
  }
  const base::ListValue* response_list;
  if (!records_v->GetAsList(&response_list)) {
    return false;
  }
  // TODO(petemill): filter out non-image articles
  std::list<mojom::ArticlePtr> articles;
  std::list<mojom::PromotedArticlePtr> promoted_articles;
  std::list<mojom::DealPtr> deals;
  std::hash<std::string> hasher;
  for (const base::Value& feed_item_raw : response_list->GetList()) {
    auto metadata = mojom::FeedItemMetadata::New();
    metadata->category_name = *feed_item_raw.FindStringKey("category");
    const char* publish_time_raw = (*feed_item_raw.FindStringKey("publish_time")).c_str();
    if (!base::Time::FromUTCString(publish_time_raw, &metadata->publish_time)) {
      LOG(ERROR) << "bad time string for feed item: ";
    }
    metadata->title = *feed_item_raw.FindStringKey("title");
    metadata->description = *feed_item_raw.FindStringKey("description");
    // TODO(petemill): relative time
    auto url_raw = *feed_item_raw.FindStringKey("url");
    auto url = GURL(url_raw);
    metadata->url = std::move(url);
    // Get hash at this point since we have a flat list, and our algorithm
    // will only change sorting.
    feed->hash = std::to_string(hasher(feed->hash + url_raw));
    auto image_url = mojom::Image::NewPaddedImageUrl(
      GURL(*feed_item_raw.FindStringKey("padded_img")));
    metadata->image = std::move(image_url);
    metadata->publisher_id = *feed_item_raw.FindStringKey("publisher_id");
    metadata->publisher_name = *feed_item_raw.FindStringKey("publisher_name");
    metadata->score = feed_item_raw.FindIntKey("score").value_or(0);
    metadata->relative_time_description = "TODO relative time";
    // Detect type
    auto content_type = *feed_item_raw.FindStringKey("content_type");
    if (content_type == "brave_partner") {
        auto item = mojom::PromotedArticle::New();
        item->creative_instance_id = *feed_item_raw.FindStringKey("creative_instance_id");
        item->data = std::move(metadata);
        promoted_articles.push_back(std::move(item));
    } else if (content_type == "product") {
        auto item = mojom::Deal::New();
        item->offers_category = *feed_item_raw.FindStringKey("offers_category");
        item->data = std::move(metadata);
        deals.push_back(std::move(item));
    } else if (content_type == "article") {
        auto item = mojom::Article::New();
        item->data = std::move(metadata);
        articles.push_back(std::move(item));
    }
    // Do not worry if unknown content_type is discovered, it could
    // be a future use.
  }
  LOG(ERROR) << "Got articles # " << articles.size();
  LOG(ERROR) << "Got deals # " << deals.size();
  LOG(ERROR) << "Got promoted articles # " << promoted_articles.size();

  // TODO(petemill): weight items via history and sort by score
  // Get unique categories present with article counts
  std::map<std::string, std::int32_t> category_counts;
  for (auto const& article : articles) {
    auto category = article->data->category_name;
    if (!category.empty() && category != "Top News") {
      auto existing_count = category_counts[category];
      category_counts[category] = existing_count + 1;
    }
  }
  // Ordered by # of occurances
  // TODO(petemill): sort map first
  // Top News is always first category
  std::vector<std::string> category_names_by_priority{"Top News"};
  for (auto kv : category_counts) {
    if (kv.first != "Top News")
      category_names_by_priority.emplace_back(kv.first);
  }
  LOG(ERROR) << "Got categories # " << category_names_by_priority.size();
  // Get unique deals categories present
  std::map<std::string, std::int32_t> deal_category_counts;
  for (auto const& deal : deals) {
    auto category = deal->offers_category;
    if (!category.empty()) {
      auto existing_count = category_counts[category];
      category_counts[category] = existing_count + 1;
    }
  }
  // Ordered by # of occurances
  // TODO(petemill): sort map first
  std::vector<std::string> deal_category_names_by_priority;
  for (auto kv : deal_category_counts) {
    deal_category_names_by_priority.emplace_back(kv.first);
  }
  LOG(ERROR) << "Got deal categories # " << deal_category_names_by_priority.size();
  // Get first headlines
  std::list<mojom::ArticlePtr>::iterator it;
  for (it = articles.begin(); it != articles.end(); it++) {
    if (it->get()->data->category_name == "Top News") {
      break;
    }
  }
  std::list<mojom::ArticlePtr> first_headlines;
  first_headlines.splice(first_headlines.begin(), articles, it);
  if (first_headlines.size() > 0) {
    // TODO(petemill): remove clone
    auto article = (*first_headlines.begin())->Clone();
    feed->featured_article = std::move(article);
  }
  // Generate as many pages of content as possible
  // Make the pages
  int cur_page = 0;
  int max_pages = 4000;
  auto category_it = category_names_by_priority.begin();
  auto deal_category_it = deal_category_names_by_priority.begin();
  auto promoted_it = promoted_articles.begin();
  while (cur_page++ < max_pages) {
    LOG(ERROR) << "Making page # " << cur_page;
    auto page = mojom::Page::New();
    // Collect headlines
    // TODO(petemill): Use the CardType type and PageContentOrder array
    // from cardsGroup.tsx here instead of having to synchronise the amount
    // of articles we take per page with how many get rendered.
    // Or generate the pages on the frontend and just provide the data in 1 array
    // here.
    size_t n_articles = 15;
    auto end = std::next(articles.begin(), std::min(n_articles, articles.size()));
    std::list<mojom::ArticlePtr> headlines;
    headlines.splice(headlines.begin(), articles, articles.begin(), end);
    if (headlines.size() == 0) {
      // No more pages of content
      LOG(ERROR) << "no more pages";
      break;
    }
    page->articles.insert(page->articles.end(),
        std::make_move_iterator(headlines.begin()),
                  std::make_move_iterator(headlines.end()));
    // Collect category
    if (category_it != category_names_by_priority.end()) {
      std::string category_name = *category_it;
      page->items_by_category = mojom::CategoryArticles::New();
      page->items_by_category->category_name = category_name;
      LOG(ERROR) << "making page for category: " << page->items_by_category->category_name << " " << category_name;
      std::list<mojom::ArticlePtr> category_articles;
      std::list<mojom::ArticlePtr>::iterator it;
      for (it = articles.begin();
             it != articles.end() && category_articles.size() <= 3;
             it++) {
        if (it->get()->data->category_name == page->items_by_category->category_name) {
          // Pass a copy of the iterator (via `it--`) so that we move
          // forward with one that hasn't been removed.
          category_articles.splice(category_articles.end(), articles, it--);
        }
      }
      LOG(ERROR) << "inserting category articles to page # " << category_articles.size();
      page->items_by_category->articles.insert(page->items_by_category->articles.end(),
          std::make_move_iterator(category_articles.begin()),
          std::make_move_iterator(category_articles.end()));
      category_it++;
    }
    LOG(ERROR) << "collecting deals if there are any";
    // Collect deals
    std::list<mojom::DealPtr> page_deals;
    auto desired_deal_count = 3u;
    if (deal_category_it != deal_category_names_by_priority.end()) {
      std::string deal_category_name = *deal_category_it;
      // Increment for next page
      deal_category_it++;
      LOG(ERROR) << "Making page of deals for cat: " << deal_category_name;
      std::list<mojom::DealPtr>::iterator it;
      for (it = deals.begin();
              it != deals.end() && page_deals.size() < desired_deal_count;
              it++) {
        if (it->get()->offers_category == deal_category_name) {
          page_deals.splice(page_deals.end(), deals, it--);
        }
      }
    }
    // Supplement with deals from other categories if we end up with fewer
    // than we want
    LOG(ERROR) << "hh";

    if (page_deals.size() < desired_deal_count) {
      auto supplemental_deal_count = std::min<uint>(
          desired_deal_count, deals.size());
      LOG(ERROR) << "Need to supplement deals # " << supplemental_deal_count;
      if (supplemental_deal_count > 0) {
        auto begin = deals.begin();
        LOG(ERROR) << "nexting";
        auto end = std::next(deals.begin(), supplemental_deal_count);
        LOG(ERROR) << "splicing";
        page_deals.splice(page_deals.end(), deals, begin, end);
      }
    }
    if (page_deals.size()) {
      LOG(ERROR) << "inserting deals # " << page_deals.size();
      std::vector<mojom::DealPtr> page_deals_v;
      page_deals_v.insert(page_deals_v.end(),
          std::make_move_iterator(page_deals.begin()),
          std::make_move_iterator(page_deals.end()));
      LOG(ERROR) << "made, moving";
      page->deals = std::move(page_deals_v);
    }
    // Items by publisher
    LOG(ERROR) << "items by publisher";
    std::string page_publisher_id;
    std::list<mojom::ArticlePtr> page_publisher_articles;
    std::list<mojom::ArticlePtr>::iterator it;
    auto desired_publisher_group_count = 3u;
    for (it = articles.begin();
            it != articles.end() &&
                page_publisher_articles.size() < desired_publisher_group_count;
            it++) {
      if (it->get()->data->publisher_id.empty()) {
        continue;
      }
      // Choose the first publisher available
      if (page_publisher_id.empty()) {
        page_publisher_id = it->get()->data->publisher_id;
      }
      // take this article and a few more
      if (it->get()->data->publisher_id == page_publisher_id) {
        page_publisher_articles.splice(
            page_publisher_articles.end(), articles, it--);
      }
    }
    LOG(ERROR) << "inserting publisher articles to page # " << page_publisher_articles.size();
    if (page_publisher_articles.size()) {
      page->items_by_publisher = mojom::PublisherArticles::New();
      page->items_by_publisher->publisher_id = page_publisher_id;
      page->items_by_publisher->articles.insert(
          page->items_by_publisher->articles.end(),
          std::make_move_iterator(page_publisher_articles.begin()),
          std::make_move_iterator(page_publisher_articles.end()));
    }
    // Promoted articles
    LOG(ERROR) << "promoted articles";
    // if (promoted_articles.size()) {
    if (promoted_it != promoted_articles.end()) {
      LOG(ERROR) << "moving a promoted article";
      // Remove 1 item at the beginning
      auto i = std::make_move_iterator(promoted_it);
      auto item = *i;
      page->promoted_article = std::move(item);
      // TODO(petemill): Clean up empty item in list and dont increment iterator
      // promoted_articles.erase(promoted_articles.begin());
      promoted_it++;
    }
    // Random articles
    // TODO: randomize
    LOG(ERROR) << "random";
    auto desired_random_articles_count = 3u;
    std::list<mojom::ArticlePtr> random_articles;
    std::list<mojom::ArticlePtr>::iterator random_it;
    for (random_it = articles.begin();
            random_it != articles.end() &&
                random_articles.size() < desired_random_articles_count;
            random_it++) {
      // TODO(petemill): Only if article within last 48hrs
      random_articles.splice(
          random_articles.end(), articles, random_it--);
    }
    if (random_articles.size()) {
      LOG(ERROR) << "random # " << random_articles.size();
      page->random_articles.insert(
          page->random_articles.end(),
          std::make_move_iterator(random_articles.begin()),
          std::make_move_iterator(random_articles.end()));
    }
    LOG(ERROR) << "done";
    feed->pages.push_back(std::move(page));
  }
  return true;
}

}  // namespace brave_news
// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/components/brave_today/browser/feed_parsing.h"

#include <algorithm>
#include <codecvt>
#include <cstdint>
#include <functional>
#include <iterator>
#include <list>
#include <random>
#include <unordered_set>
#include <utility>
#include <vector>

#include "base/json/json_reader.h"
#include "base/logging.h"
#include "base/strings/string_number_conversions.h"
#include "base/strings/stringprintf.h"
#include "base/time/time.h"
#include "brave/components/brave_today/common/brave_news.mojom.h"
#include "brave/components/brave_today/common/brave_news.mojom-forward.h"
#include "brave/components/brave_today/common/brave_news.mojom-shared.h"
#include "components/history/core/browser/history_service.h"
#include "third_party/abseil-cpp/absl/types/optional.h"
#include "ui/base/l10n/time_format.h"

namespace brave_news {

namespace {

using mojom::CardType;

std::vector<CardType> page_content_order = {
    CardType::HEADLINE,
    CardType::HEADLINE,
    CardType::HEADLINE_PAIRED,
    CardType::PROMOTED_ARTICLE,
    CardType::CATEGORY_GROUP,
    CardType::HEADLINE,
    CardType::HEADLINE,
    CardType::HEADLINE_PAIRED,
    CardType::HEADLINE_PAIRED,
    CardType::DISPLAY_AD,
    CardType::HEADLINE,
    CardType::HEADLINE,
    CardType::PUBLISHER_GROUP,
    CardType::HEADLINE_PAIRED,
    CardType::HEADLINE,
    CardType::DEALS};

std::vector<CardType> random_content_order = {
  CardType::HEADLINE,
  CardType::HEADLINE_PAIRED};

mojom::FeedItemPtr FromArticle(mojom::ArticlePtr article) {
  return mojom::FeedItem::NewArticle(std::move(article));
}

mojom::FeedItemPtr FromDeal(mojom::DealPtr deal) {
  return mojom::FeedItem::NewDeal(std::move(deal));
}

mojom::FeedItemPtr FromPromotedArticle(mojom::PromotedArticlePtr item) {
  return mojom::FeedItem::NewPromotedArticle(std::move(item));
}

bool MatchesDealsCategory(const std::string& category_name, mojom::Deal* deal) {
  return (deal->offers_category == category_name);
}

// Removes {count} items from a list and places them in a vector,
// optionally filtering via {predicate}. Wraps item in a FeedItem
// which, since it is a union type, must be created via {create}.
template<class T>
bool Take(
    size_t count, std::list<mojo::StructPtr<T>>& articles,
    std::vector<mojom::FeedItemPtr>& results,
    std::function<mojom::FeedItemPtr(mojo::StructPtr<T>)> create = FromArticle,
    std::function<bool(T*)> predicate
       = {[](T* a){return true;}}) {
  auto it = articles.begin();
  while (it != articles.end() && results.size() < count) {
    auto should_take = predicate(it->get());
    if (should_take) {
      mojom::FeedItemPtr item = create(*std::make_move_iterator(it));
      results.emplace_back(std::move(item));
      it = articles.erase(it);
    } else {
      it++;
    }
  }
  return (results.size() == count);
}

// Like Take<T> except selects randomly instead of in order.
template<class T>
void TakeRandom(
    size_t count, std::list<mojo::StructPtr<T>>& articles,
    std::vector<mojom::FeedItemPtr>& results,
    std::function<mojom::FeedItemPtr(mojo::StructPtr<T>)> create = FromArticle,
    std::function<bool(T*)> predicate
       = {[](T* a){return true;}}) {
  auto it = articles.begin();
  std::vector<typename std::list<mojo::StructPtr<T>>::iterator> matching_iterators;
  while (it != articles.end()) {
    auto should_take = predicate(it->get());
    if (should_take) {
      matching_iterators.push_back(it);
    }
    it++;
  }
  std::random_device rd;
  std::mt19937 g(rd());
  std::shuffle(matching_iterators.begin(), matching_iterators.end(), g);
  if (matching_iterators.size() > count) {
    matching_iterators.resize(count);
  }
  for (auto & matching_iterator : matching_iterators) {
    auto a = std::make_move_iterator(matching_iterator);
    auto item = create(*a);
    results.emplace_back(std::move(item));
    articles.erase(matching_iterator);
  }
}

// Decides which content to take for a specific item in the feed.
// Items approximately correspond to "cards" in the UI, although an item
// could be 2 cards (e.g. HEADLINE_PAIRED) or multiple
// articles (e.g. CATEGORY_GROUP).
void BuildFeedPageItem (std::list<mojom::ArticlePtr>& articles,
    std::list<mojom::PromotedArticlePtr>& promoted_articles,
    std::list<mojom::DealPtr>& deals,
    std::string& deal_category_name,
    std::string& article_category_name,
    bool is_random,
    mojom::FeedPageItemPtr* page_item_ptr) {
  auto* page_item = page_item_ptr->get();
  if (is_random) {
    // Additional difference for is_random is that we only consider items from
    // the last 48hrs.
    base::Time time_limit = base::Time::Now() - base::TimeDelta::FromDays(2);
    auto match_is_recent = [time_limit](mojom::Article* article) {
      return (article->data->publish_time >= time_limit);
    };
    switch (page_item->card_type) {
      case CardType::HEADLINE:
        TakeRandom<mojom::Article>(1u, articles, page_item->items, FromArticle,
            match_is_recent);
        break;
      case CardType::HEADLINE_PAIRED:
        TakeRandom<mojom::Article>(2u, articles, page_item->items, FromArticle,
            match_is_recent);
        break;
      default:
        VLOG(1) << "Card Type not handled for is_random: "
            << page_item->card_type;
        break;
    }
  }
  // Not having enough articles is the only real reason to abandon a page.
  switch (page_item->card_type) {
    case CardType::HEADLINE:
      Take<mojom::Article>(1u, articles, page_item->items, FromArticle);
      break;
    case CardType::HEADLINE_PAIRED:
      Take<mojom::Article>(2u, articles, page_item->items, FromArticle);
      break;
    case CardType::CATEGORY_GROUP:
      Take<mojom::Article>(3u, articles, page_item->items, FromArticle,
        [article_category_name](mojom::Article* article) {
            return (article->data->category_name == article_category_name);
        });
      break;
    case CardType::PUBLISHER_GROUP: {
      // Choose the first publisher available
      std::string publisher_id;
      for (auto & article : articles) {
        if (article->data->publisher_id.empty()) {
          continue;
        }
        publisher_id = article.get()->data->publisher_id;
        break;
      }
      Take<mojom::Article>(3u, articles, page_item->items, FromArticle,
          [publisher_id](mojom::Article* article) {
              return (article->data->publisher_id == publisher_id);
          });
      break;
    }
    case CardType::DEALS:
      Take<mojom::Deal>(3u, deals, page_item->items, FromDeal,
          [deal_category_name](mojom::Deal* deal) {
              return MatchesDealsCategory(deal_category_name, deal); });
      if (page_item->items.size() < 3u) {
        // Supplement with deals from other categories
        size_t supplemental_count =
            std::min(3u - page_item->items.size(), deals.size());
        Take<mojom::Deal>(
            supplemental_count, deals, page_item->items, FromDeal);
      }
      break;
    case CardType::DISPLAY_AD:
      // Content is retrieved by front-end at a time
      // closer to this item being viewed.
      break;
    case CardType::PROMOTED_ARTICLE:
      Take<mojom::PromotedArticle>(1u, promoted_articles, page_item->items,
          FromPromotedArticle);
      break;
  }
}

bool ParseFeedItem(const base::Value& feed_item_raw, Publishers* publishers,
   std::unordered_set<std::string>& history_hosts,
   std::hash<std::string>& hasher, mojom::FeedItemPtr* feed_item,
   std::string* item_hash) {
  auto url_raw = *feed_item_raw.FindStringKey("url");
  if (url_raw.empty()) {
    VLOG(1) << "Found feed item with missing url. Title: "
        << *feed_item_raw.FindStringKey("title");
    return false;
  }
  auto image_url_raw = *feed_item_raw.FindStringKey("padded_img");
  // Filter out non-image articles
  if (image_url_raw.empty()) {
    VLOG(2) << "Found feed item with missing image. Url: " << url_raw;
    return false;
  }
  // Filter out articles from publishers we're ignoring
  auto publisher_id = *feed_item_raw.FindStringKey("publisher_id");
  if (publisher_id.empty()) {
    VLOG(1) << "found article with missing publisher_id. Url: " << url_raw;
    return false;
  }
  if (!publishers->contains(publisher_id)) {
    VLOG(1) << "found article with unknown publisher_id. PublisherId: "
        << publisher_id << " Url: " << url_raw;
    return false;
  }
  if (publishers->at(publisher_id)->user_enabled_status ==
          brave_news::mojom::UserEnabled::DISABLED) {
    VLOG(1) << "Hiding article for publisher " << publisher_id << ": " << publishers->at(publisher_id)->publisher_name;
    return false;
  }
  // Get hash at this point since we have a flat list, and our algorithm
  // will only change sorting which can be re-applied on the next
  // feed update.
  item_hash->assign(url_raw);
  // Parse metadata which all content types have
  auto metadata = mojom::FeedItemMetadata::New();
  metadata->category_name = *feed_item_raw.FindStringKey("category");
  metadata->title = *feed_item_raw.FindStringKey("title");
  metadata->description = *feed_item_raw.FindStringKey("description");
  metadata->publisher_id = publisher_id;
  metadata->publisher_name = *feed_item_raw.FindStringKey("publisher_name");
  auto image_url = mojom::Image::NewPaddedImageUrl(
    GURL(image_url_raw));
  metadata->image = std::move(image_url);
  auto url = GURL(url_raw);
  if (url.is_empty() || !url.has_host()) {
    VLOG(1) << "Could not parse item url: " << url_raw;
    return false;
  }
  metadata->url = std::move(url);
  // Further weight according to history
  metadata->score = feed_item_raw.FindDoubleKey("score").value_or(0.0);
  if (history_hosts.find(metadata->url.host()) != history_hosts.end()) {
    metadata->score -= 5;
  }
  // Extract time
  const char* publish_time_raw = (*feed_item_raw.FindStringKey("publish_time")).c_str();
  if (!base::Time::FromUTCString(publish_time_raw, &metadata->publish_time)) {
    VLOG(1) << "bad time string for feed item: " << publish_time_raw;
  } else {
    // Successful, get language-specific relative time
    base::TimeDelta relative_time_delta = base::Time::Now() - metadata->publish_time;
    std::wstring_convert<
        std::codecvt_utf8_utf16<char16_t>,char16_t> converter;
    metadata->relative_time_description = converter.to_bytes(ui::TimeFormat::Simple(
        ui::TimeFormat::Format::FORMAT_ELAPSED,
        ui::TimeFormat::Length::LENGTH_LONG, relative_time_delta));
  }

  // TODO(petemill): validate
  // Detect type
  auto content_type = *feed_item_raw.FindStringKey("content_type");
  if (content_type == "brave_partner") {
      auto item = mojom::PromotedArticle::New();
      item->creative_instance_id = *feed_item_raw.FindStringKey("creative_instance_id");
      item->data = std::move(metadata);
      feed_item->get()->set_promoted_article(std::move(item));
  } else if (content_type == "product") {
      auto item = mojom::Deal::New();
      item->offers_category = *feed_item_raw.FindStringKey("offers_category");
      item->data = std::move(metadata);
      feed_item->get()->set_deal(std::move(item));
  } else if (content_type == "article") {
      auto item = mojom::Article::New();
      item->data = std::move(metadata);
      feed_item->get()->set_article(std::move(item));
  } else {
    // Do not error if unknown content_type is discovered, it could
    // be a future use.
    return false;
  }
  return true;
}

}  // namespace

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
    // TODO(petemill): Validate
    publishers->insert_or_assign(publisher->publisher_id, std::move(publisher));
  }
  return true;
}

bool ParseFeed(const std::string& json,
                        Publishers* publishers,
                        std::unordered_set<std::string>& history_hosts,
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
  std::list<mojom::ArticlePtr> articles;
  std::list<mojom::PromotedArticlePtr> promoted_articles;
  std::list<mojom::DealPtr> deals;
  std::hash<std::string> hasher;
  for (const base::Value& feed_item_raw : response_list->GetList()) {
    auto item = mojom::FeedItem::New();
    std::string item_hash;
    if (ParseFeedItem(feed_item_raw, publishers, history_hosts, hasher,
        &item, &item_hash)) {
      feed->hash = std::to_string(hasher(feed->hash + item_hash));
      switch (item->which()) {
        case mojom::FeedItem::Tag::kArticle:
          articles.push_back(std::move(item->get_article()));
          break;
        case mojom::FeedItem::Tag::kDeal:
          deals.push_back(std::move(item->get_deal()));
          break;
        case mojom::FeedItem::Tag::kPromotedArticle:
          promoted_articles.push_back(std::move(item->get_promoted_article()));
          break;
      }
    }
  }
  VLOG(1) << "Got articles # " << articles.size();
  VLOG(1) << "Got deals # " << deals.size();
  VLOG(1) << "Got promoted articles # " << promoted_articles.size();
  // Sort by score
  articles.sort([](mojom::ArticlePtr &a, mojom::ArticlePtr &b) {
    return (a.get()->data->score < b.get()->data->score);
  });
  promoted_articles.sort([](mojom::PromotedArticlePtr &a, mojom::PromotedArticlePtr &b) {
    return (a.get()->data->score < b.get()->data->score);
  });
  deals.sort([](mojom::DealPtr &a, mojom::DealPtr &b) {
    return (a.get()->data->score < b.get()->data->score);
  });
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
  std::vector<std::string> category_names_by_priority;
  for (auto kv : category_counts) {
    // Top News is always first category
    // TODO(petemill): handle translated version in non-english feeds
    if (kv.first != "Top News")
      category_names_by_priority.emplace_back(kv.first);
  }
  std::sort(category_names_by_priority.begin(),
    category_names_by_priority.end(),
    [category_counts](std::string &a, std::string &b) {
      return (category_counts.at(a) < category_counts.at(b));
    });
  // Top News is always first category
  category_names_by_priority.insert(
      category_names_by_priority.begin(), "Top News");
  VLOG(1) << "Got categories # " << category_names_by_priority.size();
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
  std::vector<std::string> deal_category_names_by_priority;
  for (auto kv : deal_category_counts) {
    deal_category_names_by_priority.emplace_back(kv.first);
  }
  std::sort(deal_category_names_by_priority.begin(),
      deal_category_names_by_priority.end(),
      [deal_category_counts](std::string &a, std::string &b) {
        return (deal_category_counts.at(a) < deal_category_counts.at(b));
      });
  VLOG(1) << "Got deal categories # " << deal_category_names_by_priority.size();
  // Get first headline
  std::list<mojom::ArticlePtr>::iterator featured_article_it;
  for (featured_article_it = articles.begin();
          featured_article_it != articles.end(); featured_article_it++) {
    if (featured_article_it->get()->data->category_name == "Top News") {
      break;
    }
  }
  if (featured_article_it != articles.end()) {
    auto item = *std::make_move_iterator(featured_article_it);
    auto article = mojom::FeedItem::NewArticle(std::move(item));
    feed->featured_item = std::move(article);
    articles.erase(featured_article_it);
  }
  // Generate as many pages of content as possible
  // Make the pages
  int cur_page = 0;
  const int max_pages = 4000;
  auto category_it = category_names_by_priority.begin();
  auto deal_category_it = deal_category_names_by_priority.begin();
  while (cur_page++ < max_pages) {
    if (articles.size() == 0) {
      // No more pages of content
      break;
    }
    std::string deal_category_name =
        (deal_category_it != deal_category_names_by_priority.end())
        ? *deal_category_it
        : "";
    std::string article_category_name =
        (category_it != category_names_by_priority.end())
        ? *category_it
        : "";
    auto feed_page = mojom::FeedPage::New();
    for (auto card_type : page_content_order) {
      auto feed_page_item = mojom::FeedPageItem::New();
      feed_page_item->card_type = card_type;
      BuildFeedPageItem(articles, promoted_articles, deals, deal_category_name,
          article_category_name, false, &feed_page_item);
      feed_page->items.push_back(std::move(feed_page_item));
    }
    for (auto card_type : random_content_order) {
      auto feed_page_item = mojom::FeedPageItem::New();
      feed_page_item->card_type = card_type;
      BuildFeedPageItem(articles, promoted_articles, deals, deal_category_name,
          article_category_name, true, &feed_page_item);
      feed_page->items.push_back(std::move(feed_page_item));
    }
    feed->pages.push_back(std::move(feed_page));
    if (!deal_category_name.empty()) {
        deal_category_it++;
      }
      if (!article_category_name.empty()) {
        category_it++;
      }
  }
  VLOG(1) << "Made pages # " << feed->pages.size();
  return true;
}

}  // namespace brave_news

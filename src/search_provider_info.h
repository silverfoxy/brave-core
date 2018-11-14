/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#pragma once

#include <string>

namespace ads {

struct SearchProviderInfo {
 public:
  SearchProviderInfo() :
    name(""),
    hostname(""),
    search_template(""),
    is_always_classed_as_a_search(false) {}

  SearchProviderInfo(
      const std::string& name,
      const std::string& hostname,
      const std::string& search_template,
      bool is_always_classed_as_a_search) :
    name(name),
    hostname(hostname),
    search_template(search_template),
    is_always_classed_as_a_search(is_always_classed_as_a_search) {}

  std::string name;
  std::string hostname;
  std::string search_template;
  bool is_always_classed_as_a_search;
};

}  // namespace ads

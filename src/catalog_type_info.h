/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#pragma once

#include <string>

namespace ads {

struct TypeInfo {
  TypeInfo() :
      code(""),
      name(""),
      platform(""),
      version(0) {}

  std::string code;
  std::string name;
  std::string platform;
  uint64_t version;
};

}  // namespace ads

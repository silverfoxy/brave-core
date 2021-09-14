/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "brave/components/brave_component_updater/browser/brave_component.h"

#include "base/command_line.h"
#include "base/test/scoped_feature_list.h"
#include "brave/components/brave_component_updater/browser/features.h"
#include "brave/components/brave_component_updater/browser/switches.h"
#include "testing/gtest/include/gtest/gtest.h"

namespace brave_component_updater {

TEST(BraveComponentUpdaterUnitTest, TestGetUpdateHost) {
  base::test::ScopedFeatureList feature_list;

  base::CommandLine& command_line = *base::CommandLine::ForCurrentProcess();
  EXPECT_EQ(GetUpdateURLHost(), UPDATER_PROD_ENDPOINT);

  command_line.AppendSwitch(brave_component_updater::kUseGoUpdateDev);
  EXPECT_EQ(GetUpdateURLHost(), UPDATER_DEV_ENDPOINT);

  command_line.RemoveSwitch(brave_component_updater::kUseGoUpdateDev);
  feature_list.InitAndEnableFeature(brave_component_updater::kUseDevUpdaterUrl);
  EXPECT_EQ(GetUpdateURLHost(), UPDATER_DEV_ENDPOINT);
}

}  // namespace brave_component_updater

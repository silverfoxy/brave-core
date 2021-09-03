/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

import { Host, HostState } from './interfaces'
import { createStateManager } from '../../shared/lib/state_manager'

const rewardsTourURL = 'brave_rewards_panel.html#tour'

export function createHost (): Host {
  const stateManager = createStateManager<HostState>({
    loading: true,
    rewardsEnabled: false
  })

  chrome.braveRewards.shouldShowOnboarding((showOnboarding) => {
    stateManager.update({
      loading: false,
      rewardsEnabled: !showOnboarding
    })
  })

  return {
    get state () { return stateManager.getState() },

    addListener: stateManager.addListener,

    getString (key) {
      return chrome.i18n.getMessage(key,
        ['$1', '$2', '$3', '$4', '$5', '$6', '$7', '$8', '$9'])
    },

    openRewardsTour () {
      window.location.href = rewardsTourURL
    },

    enableRewards () {
      chrome.braveRewards.enableRewards()
    }
  }
}

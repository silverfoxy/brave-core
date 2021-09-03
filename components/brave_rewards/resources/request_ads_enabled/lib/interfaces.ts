/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

export interface HostState {
  loading: boolean
  rewardsEnabled: boolean
}

export type HostListener = (state: HostState) => void

export interface Host {
  state: HostState
  addListener: (callback: HostListener) => (() => void)
  getString: (key: string) => string
  openRewardsTour: () => void
  enableRewards: () => void
}

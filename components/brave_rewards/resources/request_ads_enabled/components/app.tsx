/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

import * as React from 'react'

import { LocaleContext } from '../../shared/lib/locale_context'
import { HostContext, useHostListener } from '../lib/host_context'
import { Host } from '../lib/interfaces'
import { WithThemeVariables } from '../../shared/components/with_theme_variables'
import { BraveTalkOptInForm } from '../../shared/components/onboarding/brave_talk_opt_in_form'

interface Props {
  host: Host
}

export function App (props: Props) {
  const { host } = props

  const [loading, setLoading] = React.useState(host.state.loading)
  const [rewardsEnabled, setRewardsEnabled] =
    React.useState(host.state.rewardsEnabled)

  useHostListener(props.host, (state) => {
    setLoading(state.loading)
    setRewardsEnabled(state.rewardsEnabled)
  })

  if (loading) {
    return null
  }

  return (
    <HostContext.Provider value={props.host}>
      <LocaleContext.Provider value={props.host}>
        <WithThemeVariables>
          <BraveTalkOptInForm
            rewardsEnabled={rewardsEnabled}
            onEnable={host.enableRewards}
            onTakeTour={host.openRewardsTour}
          />
        </WithThemeVariables>
      </LocaleContext.Provider>
    </HostContext.Provider>
  )
}

import * as React from 'react'
import SelectRegionList from './components/select-region-list'
import MainPanel from './components/main-panel'
import ErrorPanel from './components/error-panel'
import apiProxy from './api/vpn_panel_api_proxy.js'
import { ConnectionState } from './api/panel_browser_api'
import { RegionState, Region } from './api/region_interface'

function Main () {
  const [isOn, setOn] = React.useState(false)
  const [isSelectingRegion, setSelectingRegion] = React.useState(false)
  const [hasError, setHasError] = React.useState(false)
  const [status, setStatus] = React.useState<ConnectionState>(ConnectionState.DISCONNECTED)

  /* TODO(simonhong & nullhook): Get default state, match device timezone to
  /* proper region and expose to mojom as GetDeviceRegion
  */
  const [region, setRegion] = React.useState<RegionState>({
    all: null,
    current: { continent: 'Europe', name: 'eu-de', namePretty: 'Germany' },
    hasError: false
  })

  // VPN api actions should be performed on user interactions only
  const handleToggleClick = () => {
    setOn(prevState => {
      const newState = !prevState
      if (newState) apiProxy.getInstance().connect()
      else apiProxy.getInstance().disconnect()
      return newState
    })
  }

  const handleTryAgain = () => {
    setHasError(false)
    setOn(true)
    apiProxy.getInstance().connect()
  }

  const handleSelectRegionButtonClick = () => setSelectingRegion(true)

  const resetUI = (state: boolean) => {
    if (state) {
      setOn(false)
      setHasError(false)
    }
  }

  const onSelectingRegionDone = () => {
    resetUI(hasError)
    setSelectingRegion(false)
  }

  const handleRegionItemClick = (currentRegion: Region) => {
    setRegion(prevState => ({
      ...prevState,
      current: currentRegion
    }))
  }

  React.useEffect(() => {
    const onVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        resetUI(hasError)
        apiProxy.getInstance().showUI()
      }
    }

    // TODO(nullhook): Get and set default state for current region
    const getAllRegions = async () => {
      try {
        const allRegions = await apiProxy.getInstance().getAllRegions()
        setRegion(prevState => ({ ...prevState, all: allRegions.regions }))
      } catch (err) {
        console.error(err)
        // TODO(nullhook): Create an error component for region list
        setRegion(prevState => ({ ...prevState, hasError: true }))
      }
    }

    document.addEventListener('visibilitychange', onVisibilityChange)

    apiProxy.getInstance().addVPNObserver({
      onConnectionCreated: () => {/**/},
      onConnectionRemoved: () => {/**/},
      onConnectionStateChanged: (state: ConnectionState) => {
        if (state === ConnectionState.CONNECT_FAILED) {
          setHasError(true)
          setOn(false)
        }

        setStatus(state)
      }
    })

    getAllRegions().catch(e => console.error('getAllRegions failed', e))

    return () => {
      document.removeEventListener('visibilitychange', onVisibilityChange)
    }
  }, [hasError])

  React.useEffect(() => {
    const getInitialState = async () => {
      const res = await apiProxy.getInstance().getConnectionState()
      setOn(res.state === ConnectionState.CONNECTED
        || res.state === ConnectionState.CONNECTING)
      // Treat connection failure as disconnect on initial startup
      if (res.state !== ConnectionState.CONNECT_FAILED) setStatus(res.state)
    }

    getInitialState().catch(e => console.error('getConnectionState failed', e))
  }, [])

  if (isSelectingRegion) {
    return (
      <SelectRegionList
        regions={region.all}
        selectedRegion={region.current}
        onDone={onSelectingRegionDone}
        onRegionClick={handleRegionItemClick}
      />)
  }

  if (hasError) {
    return (
      <ErrorPanel
        onTryAgainClick={handleTryAgain}
        onChooseServerClick={handleSelectRegionButtonClick}
        region={region}
      />
    )
  }

  return (
    <MainPanel
      isOn={isOn}
      status={status}
      region={region}
      onToggleClick={handleToggleClick}
      onSelectRegionButtonClick={handleSelectRegionButtonClick}
    />
  )
}

export default Main

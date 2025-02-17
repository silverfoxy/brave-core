import * as React from 'react'

import { StyledWrapper } from './style'
import {
  TopTabNavTypes,
  AppObjectType,
  AppsListType,
  PriceDataObjectType,
  AccountAssetOptionType,
  TransactionListInfo,
  AssetPriceInfo,
  WalletAccountType,
  AssetPriceTimeframe,
  EthereumChain,
  TokenInfo,
  UpdateAccountNamePayloadType
} from '../../../../constants/types'
import { TopNavOptions } from '../../../../options/top-nav-options'
import { TopTabNav, BackupWarningBanner, AddAccountModal } from '../../'
import { SearchBar, AppList } from '../../../shared'
import locale from '../../../../constants/locale'
import { AppsList } from '../../../../options/apps-list-options'
import { filterAppList } from '../../../../utils/filter-app-list'
import { PortfolioView, AccountsView } from '../'
import {
  HardwareWalletAccount,
  HardwareWalletConnectOpts
} from '../../popup-modals/add-account-modal/hardware-wallet-connect/types'

export interface Props {
  onLockWallet: () => void
  onShowBackup: () => void
  onChangeTimeline: (path: AssetPriceTimeframe) => void
  onSelectAsset: (asset: TokenInfo | undefined) => void
  onCreateAccount: (name: string) => void
  onImportAccount: (accountName: string, privateKey: string) => void
  onConnectHardwareWallet: (opts: HardwareWalletConnectOpts) => Promise<HardwareWalletAccount[]>
  onAddHardwareAccounts: (selected: HardwareWalletAccount[]) => void
  onUpdateAccountName: (payload: UpdateAccountNamePayloadType) => { success: boolean }
  onToggleAddModal: () => void
  onUpdateVisibleTokens: (contractAddress: string, visible: boolean) => void
  onSelectNetwork: (network: EthereumChain) => void
  fetchFullTokenList: () => void
  onRemoveAccount: (address: string) => void
  onViewPrivateKey: (address: string, isDefault: boolean) => void
  onDoneViewingPrivateKey: () => void
  onImportAccountFromJson: (accountName: string, password: string, json: string) => void
  onAddCustomToken: (tokenName: string, tokenSymbol: string, tokenContractAddress: string, tokenDecimals: number) => void
  onSetImportError: (hasError: boolean) => void
  hasImportError: boolean
  transactionSpotPrices: AssetPriceInfo[]
  privateKey: string
  fullAssetList: TokenInfo[]
  userVisibleTokensInfo: TokenInfo[]
  needsBackup: boolean
  accounts: WalletAccountType[]
  networkList: EthereumChain[]
  selectedTimeline: AssetPriceTimeframe
  selectedPortfolioTimeline: AssetPriceTimeframe
  portfolioPriceHistory: PriceDataObjectType[]
  selectedAssetPriceHistory: PriceDataObjectType[]
  selectedUSDAssetPrice: AssetPriceInfo | undefined
  selectedBTCAssetPrice: AssetPriceInfo | undefined
  selectedAsset: TokenInfo | undefined
  portfolioBalance: string
  transactions: (TransactionListInfo | undefined)[]
  userAssetList: AccountAssetOptionType[]
  userWatchList: string[]
  isLoading: boolean
  showAddModal: boolean
  selectedNetwork: EthereumChain
  isFetchingPortfolioPriceHistory: boolean
}

const CryptoView = (props: Props) => {
  const {
    onLockWallet,
    onShowBackup,
    onChangeTimeline,
    onSelectAsset,
    onCreateAccount,
    onConnectHardwareWallet,
    onAddHardwareAccounts,
    onImportAccount,
    onUpdateAccountName,
    onUpdateVisibleTokens,
    fetchFullTokenList,
    onSelectNetwork,
    onToggleAddModal,
    onRemoveAccount,
    onViewPrivateKey,
    onDoneViewingPrivateKey,
    onImportAccountFromJson,
    onSetImportError,
    onAddCustomToken,
    hasImportError,
    userVisibleTokensInfo,
    transactionSpotPrices,
    privateKey,
    selectedNetwork,
    fullAssetList,
    portfolioPriceHistory,
    userAssetList,
    userWatchList,
    selectedTimeline,
    selectedPortfolioTimeline,
    selectedAssetPriceHistory,
    needsBackup,
    accounts,
    networkList,
    selectedAsset,
    portfolioBalance,
    transactions,
    selectedUSDAssetPrice,
    selectedBTCAssetPrice,
    isLoading,
    showAddModal,
    isFetchingPortfolioPriceHistory
  } = props
  const [selectedTab, setSelectedTab] = React.useState<TopTabNavTypes>('portfolio')
  const [favoriteApps, setFavoriteApps] = React.useState<AppObjectType[]>([
    AppsList[0].appList[0]
  ])
  const [filteredAppsList, setFilteredAppsList] = React.useState<AppsListType[]>(AppsList)
  const [hideNav, setHideNav] = React.useState<boolean>(false)
  const [showBackupWarning, setShowBackupWarning] = React.useState<boolean>(needsBackup)

  // In the future these will be actual paths
  // for example wallet/crypto/portfolio
  const tabTo = (path: TopTabNavTypes) => {
    setSelectedTab(path)
  }

  const browseMore = () => {
    alert('Will expand to view more!')
  }

  const addToFavorites = (app: AppObjectType) => {
    const newList = [...favoriteApps, app]
    setFavoriteApps(newList)
  }
  const removeFromFavorites = (app: AppObjectType) => {
    const newList = favoriteApps.filter(
      (fav) => fav.name !== app.name
    )
    setFavoriteApps(newList)
  }

  const filterList = (event: any) => {
    filterAppList(event, AppsList, setFilteredAppsList)
  }

  const toggleNav = () => {
    setHideNav(!hideNav)
  }

  const onDismissBackupWarning = () => {
    setShowBackupWarning(false)
  }

  const onClickAddAccount = () => {
    onToggleAddModal()
  }

  const onCloseAddModal = () => {
    onToggleAddModal()
  }

  return (
    <StyledWrapper>
      {!hideNav &&
        <>
          <TopTabNav
            tabList={TopNavOptions}
            selectedTab={selectedTab}
            onSubmit={tabTo}
            hasMoreButtons={true}
            onLockWallet={onLockWallet}
          />
          {needsBackup && showBackupWarning &&
            <BackupWarningBanner
              onDismiss={onDismissBackupWarning}
              onBackup={onShowBackup}
            />
          }
        </>
      }
      {selectedTab === 'apps' &&
        <>
          <SearchBar
            placeholder={locale.searchText}
            action={filterList}
          />
          <AppList
            list={filteredAppsList}
            favApps={favoriteApps}
            addToFav={addToFavorites}
            removeFromFav={removeFromFavorites}
            action={browseMore}
          />
        </>
      }
      {selectedTab === 'portfolio' &&
        <PortfolioView
          toggleNav={toggleNav}
          accounts={accounts}
          networkList={networkList}
          onChangeTimeline={onChangeTimeline}
          selectedAssetPriceHistory={selectedAssetPriceHistory}
          selectedTimeline={selectedTimeline}
          selectedPortfolioTimeline={selectedPortfolioTimeline}
          onSelectAsset={onSelectAsset}
          onClickAddAccount={onClickAddAccount}
          onSelectNetwork={onSelectNetwork}
          onUpdateVisibleTokens={onUpdateVisibleTokens}
          fetchFullTokenList={fetchFullTokenList}
          selectedAsset={selectedAsset}
          portfolioBalance={portfolioBalance}
          portfolioPriceHistory={portfolioPriceHistory}
          transactions={transactions}
          selectedUSDAssetPrice={selectedUSDAssetPrice}
          selectedBTCAssetPrice={selectedBTCAssetPrice}
          userAssetList={userAssetList}
          isLoading={isLoading}
          selectedNetwork={selectedNetwork}
          fullAssetList={fullAssetList}
          userVisibleTokensInfo={userVisibleTokensInfo}
          userWatchList={userWatchList}
          isFetchingPortfolioPriceHistory={isFetchingPortfolioPriceHistory}
          onAddCustomToken={onAddCustomToken}
          transactionSpotPrices={transactionSpotPrices}
        />
      }
      {selectedTab === 'accounts' &&
        <AccountsView
          toggleNav={toggleNav}
          accounts={accounts}
          onClickBackup={onShowBackup}
          onClickAddAccount={onClickAddAccount}
          onUpdateAccountName={onUpdateAccountName}
          onRemoveAccount={onRemoveAccount}
          onDoneViewingPrivateKey={onDoneViewingPrivateKey}
          onViewPrivateKey={onViewPrivateKey}
          privateKey={privateKey}
          transactions={transactions}
          selectedNetwork={selectedNetwork}
          transactionSpotPrices={transactionSpotPrices}
          userVisibleTokensInfo={userVisibleTokensInfo}
        />
      }
      {showAddModal &&
        <AddAccountModal
          accounts={accounts}
          title={locale.addAccount}
          onClose={onCloseAddModal}
          onCreateAccount={onCreateAccount}
          onImportAccount={onImportAccount}
          onConnectHardwareWallet={onConnectHardwareWallet}
          onAddHardwareAccounts={onAddHardwareAccounts}
          onImportAccountFromJson={onImportAccountFromJson}
          hasImportError={hasImportError}
          onSetImportError={onSetImportError}
        />
      }
    </StyledWrapper>
  )
}

export default CryptoView

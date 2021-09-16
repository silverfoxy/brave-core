// @ts-ignore
import contractMap from '@metamask/contract-metadata'

import { AccountAssetOptionType, AssetOptionType, TokenInfo } from '../constants/types'
import {
  ALGOIconUrl,
  BATIconUrl,
  BNBIconUrl,
  BTCIconUrl,
  ETHIconUrl,
  ZRXIconUrl
} from '../assets/asset-icons'

export const AssetOptions: AssetOptionType[] = [
  {
    id: '1',
    name: 'Ethereum',
    symbol: 'ETH',
    icon: ETHIconUrl
  },
  {
    id: '2',
    name: 'Basic Attention Token',
    symbol: 'BAT',
    icon: BATIconUrl
  },
  {
    id: '3',
    name: 'Binance Coin',
    symbol: 'BNB',
    icon: BNBIconUrl
  },
  {
    id: '4',
    name: 'Bitcoin',
    symbol: 'BTC',
    icon: BTCIconUrl
  },
  {
    id: '5',
    name: 'Algorand',
    symbol: 'ALGO',
    icon: ALGOIconUrl
  },
  {
    id: '6',
    name: '0x',
    symbol: 'ZRX',
    icon: ZRXIconUrl
  }
]

export const NewAssetOptions: TokenInfo[] = [
  {
    contractAddress: '1',
    name: 'Ethereum',
    symbol: 'ETH',
    icon: ETHIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  },
  {
    contractAddress: '2',
    name: 'Basic Attention Token',
    symbol: 'BAT',
    icon: BATIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  },
  {
    contractAddress: '3',
    name: 'Binance Coin',
    symbol: 'BNB',
    icon: BNBIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  },
  {
    contractAddress: '4',
    name: 'Bitcoin',
    symbol: 'BTC',
    icon: BTCIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  },
  {
    contractAddress: '5',
    name: 'Algorand',
    symbol: 'ALGO',
    icon: ALGOIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  },
  {
    contractAddress: '6',
    name: '0x',
    symbol: 'ZRX',
    icon: ZRXIconUrl,
    isErc20: true,
    isErc721: false,
    decimals: 18
  }
]

export const AccountAssetOptions: AccountAssetOptionType[] = [
  {
    asset: {
      contractAddress: '1',
      name: 'Ethereum',
      symbol: 'ETH',
      icon: ETHIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  {
    asset: {
      contractAddress: '2',
      name: 'Basic Attention Token',
      symbol: 'BAT',
      icon: BATIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  {
    asset: {
      contractAddress: '3',
      name: 'Binance Coin',
      symbol: 'BNB',
      icon: BNBIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  {
    asset: {
      contractAddress: '4',
      name: 'Bitcoin',
      symbol: 'BTC',
      icon: BTCIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  {
    asset: {
      contractAddress: '5',
      name: 'Algorand',
      symbol: 'ALGO',
      icon: ALGOIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  {
    asset: {
      contractAddress: '6',
      name: '0x',
      symbol: 'ZRX',
      icon: ZRXIconUrl,
      isErc20: true,
      isErc721: false,
      decimals: 8
    },
    assetBalance: '0',
    fiatBalance: '0'
  }
]

interface ContractMetadata {
  [contractAddress: string]: {
    name: string,
    symbol: string,
    logo: string,
    erc20: boolean,
    decimals: number
  }
}

const swapTokens: AccountAssetOptionType[] = Object.entries(contractMap as ContractMetadata).map(
  ([contractAddress, { name, symbol, decimals, logo, erc20 }]) => ({
    asset: {
      contractAddress,
      name: name,
      symbol: symbol,
      icon: require(`@metamask/contract-metadata/images/${logo}`),
      isErc20: erc20,
      isErc721: false,
      decimals: decimals
    },
    assetBalance: '0',
    fiatBalance: '0'
  })
)

swapTokens.sort(function (a: AccountAssetOptionType, b: AccountAssetOptionType) {
  const first = a.asset.name.toLocaleLowerCase()
  const second = b.asset.name.toLocaleLowerCase()

  if (first < second) {
    return -1
  } else if (first > second) {
    return 1
  }

  return 0
})

export const SwapAssetOptions: AccountAssetOptionType[] = [
  {
    asset: {
      contractAddress: '0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee',  // convention followed by 0x
      name: 'Ethereum',
      symbol: 'ETH',
      icon: ETHIconUrl,
      isErc20: false,
      isErc721: false,
      decimals: 18
    },
    assetBalance: '0',
    fiatBalance: '0'
  },
  ...swapTokens
]

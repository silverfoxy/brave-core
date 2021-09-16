import { kMainnetChainId, kRopstenChainId } from './types'

/**
 * CAUTION: The contents of this file have business implications. Please check
 * with @onyb or @bbondy before modifying.
 */
export default function getSwapConfig (networkChainId: string) {
  switch (networkChainId) {
    case kRopstenChainId:
      return {
        swapAPIURL: 'https://ropsten.api.0x.org/swap/v1',
        buyTokenPercentageFee: '0.00875',
        feeRecipient: '0xa92D461a9a988A7f11ec285d39783A637Fdd6ba4',
      }

    case kMainnetChainId:
    default:
      return {
        swapAPIURL: 'https://api.0x.org/swap/v1',
        buyTokenPercentageFee: '0.00875',
        feeRecipient: '0xbd9420A98a7Bd6B89765e5715e169481602D9c3d',
      }
  }
}

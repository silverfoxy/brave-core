/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.crypto_wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.chromium.brave_wallet.mojom.AccountInfo;
import org.chromium.brave_wallet.mojom.ErcTokenRegistry;
import org.chromium.brave_wallet.mojom.EthJsonRpcController;
import org.chromium.brave_wallet.mojom.EthTxController;
import org.chromium.brave_wallet.mojom.KeyringController;
import org.chromium.brave_wallet.mojom.KeyringInfo;
import org.chromium.brave_wallet.mojom.TxData;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.crypto_wallet.ERCTokenRegistryFactory;
import org.chromium.chrome.browser.crypto_wallet.EthJsonRpcControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.EthTxControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.KeyringControllerFactory;
import org.chromium.chrome.browser.crypto_wallet.adapters.AccountSpinnerAdapter;
import org.chromium.chrome.browser.crypto_wallet.adapters.WalletCoinAdapter;
import org.chromium.chrome.browser.crypto_wallet.fragments.EditVisibleAssetsBottomSheetDialogFragment;
import org.chromium.chrome.browser.crypto_wallet.util.Utils;
import org.chromium.chrome.browser.init.AsyncInitializationActivity;
import org.chromium.mojo.bindings.ConnectionErrorHandler;
import org.chromium.mojo.system.MojoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuySendSwapActivity extends AsyncInitializationActivity
        implements ConnectionErrorHandler, AdapterView.OnItemSelectedListener {
    public enum ActivityType {
        BUY(0),
        SEND(1),
        SWAP(2);

        private int value;
        private static Map map = new HashMap<>();

        private ActivityType(int value) {
            this.value = value;
        }

        static {
            for (ActivityType activityType : ActivityType.values()) {
                map.put(activityType.value, activityType);
            }
        }

        public static ActivityType valueOf(int activityType) {
            return (ActivityType) map.get(activityType);
        }

        public int getValue() {
            return value;
        }
    }

    private ErcTokenRegistry mErcTokenRegistry;
    private EthJsonRpcController mEthJsonRpcController;
    private EthTxController mEthTxController;
    private KeyringController mKeyringController;
    private ActivityType mActivityType;
    private AccountSpinnerAdapter mCustomAccountAdapter;
    private double mConvertedBalance;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void triggerLayoutInflation() {
        setContentView(R.layout.activity_buy_send_swap);

        Intent intent = getIntent();
        mActivityType = ActivityType.valueOf(
                intent.getIntExtra("activityType", ActivityType.BUY.getValue()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView fromValueText = findViewById(R.id.from_value_text);
        fromValueText.setText("");
        fromValueText.setHint("0");

        TextView fromBalanceText = findViewById(R.id.from_balance_text);
        fromBalanceText.setText("Balance: 1.2832");

        TextView fromAssetText = findViewById(R.id.from_asset_text);
        fromAssetText.setText("ETH");

        EditText toValueText = findViewById(R.id.to_value_text);
        toValueText.setText("561.121");

        TextView toBalanceText = findViewById(R.id.to_balance_text);
        toBalanceText.setText("Balance: 0");

        TextView toAssetText = findViewById(R.id.to_asset_text);
        toAssetText.setText("ETH");

        TextView marketPriceValueText = findViewById(R.id.market_price_value_text);
        marketPriceValueText.setText("0.0005841");

        TextView slippingToleranceValueText = findViewById(R.id.slipping_tolerance_value_text);
        slippingToleranceValueText.setText("2%");

        Spinner spinner = findViewById(R.id.network_spinner);
        spinner.setOnItemSelectedListener(this);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Utils.getNetworksList(this));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        onInitialLayoutInflationComplete();

        adjustControls();
        InitErcTokenRegistry();
        InitEthJsonRpcController();
        InitEthTxController();
        InitKeyringController();

        if (mEthJsonRpcController != null) {
            mEthJsonRpcController.getChainId(
                    chainId -> { spinner.setSelection(getIndexOf(spinner, chainId)); });
        }
        if (mKeyringController != null) {
            mKeyringController.getDefaultKeyringInfo(keyring -> {
                int[] pictures = new int[keyring.accountInfos.length];
                String[] accountNames = new String[keyring.accountInfos.length];
                String[] accountTitles = new String[keyring.accountInfos.length];
                int currentPos = 0;
                for (AccountInfo info : keyring.accountInfos) {
                    pictures[currentPos] = R.drawable.ic_eth_24;
                    accountNames[currentPos] = info.name;
                    accountTitles[currentPos] = info.address;
                    currentPos++;
                }
                Spinner accountSpinner = findViewById(R.id.accounts_spinner);
                mCustomAccountAdapter = new AccountSpinnerAdapter(
                        getApplicationContext(), pictures, accountNames, accountTitles);
                accountSpinner.setAdapter(mCustomAccountAdapter);
                accountSpinner.setOnItemSelectedListener(this);
                if (accountTitles.length > 0) {
                    updateBalance(accountTitles[accountTitles.length - 1]);
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.network_spinner) {
            String item = parent.getItemAtPosition(position).toString();
            if (mEthJsonRpcController != null) {
                mEthJsonRpcController.setNetwork(Utils.getNetworkConst(this, item));
            }
            Spinner accountSpinner = findViewById(R.id.accounts_spinner);
            updateBalance(mCustomAccountAdapter.getTitleAtPosition(
                    accountSpinner.getSelectedItemPosition()));
        } else if (parent.getId() == R.id.accounts_spinner) {
            updateBalance(mCustomAccountAdapter.getTitleAtPosition(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}

    private void updateBalance(String address) {
        if (mEthJsonRpcController == null) {
            return;
        }
        mEthJsonRpcController.getBalance(address, (success, balance) -> {
            if (!success) {
                return;
            }
            TextView fromBalanceText = findViewById(R.id.from_balance_text);
            mConvertedBalance = Utils.fromHexWei(balance);
            fromBalanceText.setText(getText(R.string.crypto_wallet_balance) + " "
                    + String.format("%.4f", mConvertedBalance));
        });
    }

    private int getIndexOf(Spinner spinner, String chainId) {
        String strNetwork = Utils.getNetworkText(this, chainId).toString();
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(strNetwork)) {
                return i;
            }
        }

        return 0;
    }

    private void adjustControls() {
        EditText toValueText = findViewById(R.id.to_value_text);
        TextView marketPriceValueText = findViewById(R.id.market_price_value_text);
        TextView slippingToleranceValueText = findViewById(R.id.slipping_tolerance_value_text);
        RadioGroup radioBuySendSwap = findViewById(R.id.buy_send_swap_type_radio_group);
        LinearLayout marketPriceSection = findViewById(R.id.market_price_section);
        LinearLayout toleranceSection = findViewById(R.id.tolerance_section);
        Button btnBuySendSwap = findViewById(R.id.btn_buy_send_swap);
        TextView currencySign = findViewById(R.id.currency_sign);
        TextView toEstimateText = findViewById(R.id.to_estimate_text);
        TextView assetDropDown = findViewById(R.id.from_asset_text);
        RadioGroup radioPerPercent = findViewById(R.id.per_percent_radiogroup);
        radioPerPercent.clearCheck();
        if (mActivityType == ActivityType.BUY) {
            TextView fromBuyText = findViewById(R.id.from_buy_text);
            fromBuyText.setText(getText(R.string.buy_wallet));
            ImageView arrowDown = findViewById(R.id.arrow_down);
            arrowDown.setVisibility(View.GONE);
            LinearLayout toSection = findViewById(R.id.to_section);
            toSection.setVisibility(View.GONE);
            radioBuySendSwap.setVisibility(View.GONE);
            marketPriceSection.setVisibility(View.GONE);
            toleranceSection.setVisibility(View.GONE);
            btnBuySendSwap.setText(getText(R.string.buy_wallet));
            radioPerPercent.setVisibility(View.GONE);
            assetDropDown.setOnClickListener(v -> {
                EditVisibleAssetsBottomSheetDialogFragment bottomSheetDialogFragment =
                        EditVisibleAssetsBottomSheetDialogFragment.newInstance(
                                WalletCoinAdapter.AdapterType.BUY_ASSETS_LIST);
                bottomSheetDialogFragment.show(getSupportFragmentManager(),
                        EditVisibleAssetsBottomSheetDialogFragment.TAG_FRAGMENT);
            });
        } else if (mActivityType == ActivityType.SEND) {
            currencySign.setVisibility(View.GONE);
            toEstimateText.setText(getText(R.string.to_address));
            toValueText.setText("");
            toValueText.setHint(getText(R.string.to_address_edit));
            radioBuySendSwap.setVisibility(View.GONE);
            marketPriceSection.setVisibility(View.GONE);
            toleranceSection.setVisibility(View.GONE);
            btnBuySendSwap.setText(getText(R.string.send));
            LinearLayout toBalanceSection = findViewById(R.id.to_balance_section);
            toBalanceSection.setVisibility(View.GONE);
            assetDropDown.setOnClickListener(v -> {
                EditVisibleAssetsBottomSheetDialogFragment bottomSheetDialogFragment =
                        EditVisibleAssetsBottomSheetDialogFragment.newInstance(
                                WalletCoinAdapter.AdapterType.SEND_ASSETS_LIST);
                bottomSheetDialogFragment.show(getSupportFragmentManager(),
                        EditVisibleAssetsBottomSheetDialogFragment.TAG_FRAGMENT);
            });
        } else if (mActivityType == ActivityType.SWAP) {
            currencySign.setVisibility(View.GONE);
            toValueText.setText("");
            toValueText.setHint("0");
            Button btMarket = findViewById(R.id.market_radio);
            Button btLimit = findViewById(R.id.limit_radio);
            TextView marketLimitPriceText = findViewById(R.id.market_limit_price_text);
            EditText limitPriceValue = findViewById(R.id.limit_price_value);
            TextView slippingExpiresValueText = findViewById(R.id.slipping_expires_value_text);
            ImageView refreshPrice = findViewById(R.id.refresh_price);
            btMarket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toEstimateText.setText(getText(R.string.to_estimate));
                    marketLimitPriceText.setText(getText(R.string.market_price_in));
                    marketPriceValueText.setVisibility(View.VISIBLE);
                    limitPriceValue.setVisibility(View.GONE);
                    marketPriceValueText.setVisibility(View.VISIBLE);
                    slippingExpiresValueText.setText(getText(R.string.slipping_tolerance));
                    slippingToleranceValueText.setText("2%");
                    refreshPrice.setVisibility(View.VISIBLE);
                }
            });
            btLimit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toEstimateText.setText(getText(R.string.to_address));
                    marketLimitPriceText.setText(getText(R.string.price_in));
                    marketPriceValueText.setVisibility(View.GONE);
                    limitPriceValue.setVisibility(View.VISIBLE);
                    marketPriceValueText.setVisibility(View.GONE);
                    slippingExpiresValueText.setText(getText(R.string.expires_in));
                    slippingToleranceValueText.setText("1 days");
                    refreshPrice.setVisibility(View.GONE);
                }
            });
        }

        btnBuySendSwap.setOnClickListener(v -> {
            if (mActivityType == ActivityType.SEND) {
                // TODO(sergz): token transfers via a token contract
                String to = toValueText.getText().toString();
                if (to.isEmpty()) {
                    // TODO(sergz): some address validation
                    return;
                }
                TextView fromValueText = findViewById(R.id.from_value_text);
                String value = fromValueText.getText().toString();
                // TODO(sergz): Some kind of validation that we have enough balance
                // TODO(sergz): Get gas price
                TxData data = Utils.getTxData("0x1", "0x2FBF9BEDA5F0", "0x2FBF9BEDA5F1", to,
                        Utils.toHexWei(value), new byte[0]);
                Spinner accountSpinner = findViewById(R.id.accounts_spinner);
                String from = mCustomAccountAdapter.getTitleAtPosition(
                        accountSpinner.getSelectedItemPosition());
                if (mEthTxController != null) {
                    mEthTxController.addUnapprovedTransaction(data, from,
                            (success, tx_meta_id, error_message)
                                    -> {
                                            // TODO(sergz): Add an observer and approve transaction
                                    });
                }
            }
        });

        radioPerPercent.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == -1) {
                    return;
                }
                double amountToGet = 0;
                if (checkedId == R.id.per_25_radiobutton) {
                    amountToGet = 0.25 * mConvertedBalance;
                } else if (checkedId == R.id.per_50_radiobutton) {
                    amountToGet = 0.5 * mConvertedBalance;
                } else if (checkedId == R.id.per_75_radiobutton) {
                    amountToGet = 0.75 * mConvertedBalance;
                } else {
                    amountToGet = mConvertedBalance;
                }

                TextView fromValueText = findViewById(R.id.from_value_text);
                fromValueText.setText(String.format("%f", amountToGet));
                radioPerPercent.clearCheck();
            }
        });
    }

    public void updateBuySendAsset(String asset) {
        TextView assetDropDown = findViewById(R.id.from_asset_text);
        assetDropDown.setText(asset);
    }

    public ErcTokenRegistry getErcTokenRegistry() {
        return mErcTokenRegistry;
    }

    @Override
    public void onConnectionError(MojoException e) {
        mErcTokenRegistry = null;
        mEthJsonRpcController = null;
        mEthTxController = null;
        mKeyringController = null;
        InitErcTokenRegistry();
        InitEthJsonRpcController();
        InitEthTxController();
        InitKeyringController();
    }

    private void InitKeyringController() {
        if (mKeyringController != null) {
            return;
        }

        mKeyringController = KeyringControllerFactory.getInstance().getKeyringController(this);
    }

    private void InitErcTokenRegistry() {
        if (mErcTokenRegistry != null) {
            return;
        }

        mErcTokenRegistry = ERCTokenRegistryFactory.getInstance().getERCTokenRegistry(this);
    }

    private void InitEthJsonRpcController() {
        if (mEthJsonRpcController != null) {
            return;
        }

        mEthJsonRpcController =
                EthJsonRpcControllerFactory.getInstance().getEthJsonRpcController(this);
    }

    private void InitEthTxController() {
        if (mEthTxController != null) {
            return;
        }

        mEthTxController = EthTxControllerFactory.getInstance().getEthTxController(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finishNativeInitialization() {
        super.finishNativeInitialization();
    }

    @Override
    public boolean shouldStartGpuProcess() {
        return true;
    }
}

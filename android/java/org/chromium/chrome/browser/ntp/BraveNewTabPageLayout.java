/* Copyright (c) 2019 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.ntp;

import static org.chromium.ui.base.ViewUtils.dpToPx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.base.ThreadUtils;
import org.chromium.base.TraceEvent;
import org.chromium.base.supplier.Supplier;
import org.chromium.base.task.AsyncTask;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.BraveAdsNativeHelper;
import org.chromium.chrome.browser.BraveRewardsHelper;
import org.chromium.chrome.browser.ChromeTabbedActivity;
import org.chromium.chrome.browser.InternetConnection;
import org.chromium.chrome.browser.QRCodeShareDialogFragment;
import org.chromium.chrome.browser.app.BraveActivity;
import org.chromium.chrome.browser.brave_news.BraveNewsAdapter;
import org.chromium.chrome.browser.brave_news.BraveNewsUtils;
import org.chromium.chrome.browser.brave_news.models.NewsItem;
import org.chromium.chrome.browser.brave_stats.BraveStatsUtil;
import org.chromium.chrome.browser.compositor.CompositorViewHolder;
import org.chromium.chrome.browser.compositor.layouts.OverviewModeBehavior;
import org.chromium.chrome.browser.custom_layout.VerticalViewPager;
import org.chromium.chrome.browser.explore_sites.ExploreSitesBridge;
import org.chromium.chrome.browser.lifecycle.ActivityLifecycleDispatcher;
import org.chromium.chrome.browser.local_database.DatabaseHelper;
import org.chromium.chrome.browser.local_database.TopSiteTable;
import org.chromium.chrome.browser.native_page.ContextMenuManager;
import org.chromium.chrome.browser.night_mode.GlobalNightModeStateProviderHolder;
import org.chromium.chrome.browser.ntp.NewTabPageLayout;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetAdapter;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetItem;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetManager;
import org.chromium.chrome.browser.ntp.widget.NTPWidgetStackActivity;
import org.chromium.chrome.browser.ntp_background_images.NTPBackgroundImagesBridge;
import org.chromium.chrome.browser.ntp_background_images.model.BackgroundImage;
import org.chromium.chrome.browser.ntp_background_images.model.NTPImage;
import org.chromium.chrome.browser.ntp_background_images.model.SponsoredTab;
import org.chromium.chrome.browser.ntp_background_images.model.TopSite;
import org.chromium.chrome.browser.ntp_background_images.model.Wallpaper;
import org.chromium.chrome.browser.ntp_background_images.util.FetchWallpaperWorkerTask;
import org.chromium.chrome.browser.ntp_background_images.util.NTPUtil;
import org.chromium.chrome.browser.ntp_background_images.util.NewTabPageListener;
import org.chromium.chrome.browser.ntp_background_images.util.SponsoredImageUtil;
import org.chromium.chrome.browser.offlinepages.DownloadUiActionFlags;
import org.chromium.chrome.browser.offlinepages.OfflinePageBridge;
import org.chromium.chrome.browser.offlinepages.RequestCoordinatorBridge;
import org.chromium.chrome.browser.onboarding.OnboardingPrefManager;
import org.chromium.chrome.browser.preferences.BravePref;
import org.chromium.chrome.browser.preferences.BravePrefServiceBridge;
import org.chromium.chrome.browser.profiles.Profile;

import org.chromium.chrome.browser.settings.BraveNewsPreferences;
import org.chromium.chrome.browser.settings.SettingsLauncherImpl;
import org.chromium.chrome.browser.suggestions.tile.SiteSection;
import org.chromium.chrome.browser.suggestions.tile.TileGroup;
import org.chromium.chrome.browser.sync.settings.BraveManageSyncSettings;
import org.chromium.chrome.browser.tab.EmptyTabObserver;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.tab.TabAttributes;
import org.chromium.chrome.browser.tab.TabImpl;
import org.chromium.chrome.browser.tab.TabObserver;
import org.chromium.chrome.browser.tabmodel.TabModel;
import org.chromium.chrome.browser.util.ConfigurationUtils;
import org.chromium.chrome.browser.util.PackageUtils;
import org.chromium.chrome.browser.util.TabUtils;
import org.chromium.chrome.browser.widget.crypto.binance.BinanceAccountBalance;
import org.chromium.chrome.browser.widget.crypto.binance.BinanceNativeWorker;
import org.chromium.chrome.browser.widget.crypto.binance.BinanceObserver;
import org.chromium.chrome.browser.widget.crypto.binance.BinanceWidgetManager;
import org.chromium.chrome.browser.widget.crypto.binance.CryptoWidgetBottomSheetDialogFragment;
import org.chromium.components.browser_ui.settings.SettingsLauncher;
import org.chromium.components.browser_ui.widget.displaystyle.UiConfig;
import org.chromium.components.embedder_support.util.UrlUtilities;
import org.chromium.components.user_prefs.UserPrefs;
import org.chromium.ui.base.WindowAndroid;
import org.chromium.ui.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BraveNewTabPageLayout
        extends NewTabPageLayout implements CryptoWidgetBottomSheetDialogFragment
                                                    .CryptoWidgetBottomSheetDialogDismissListener,
                                            BraveNewsAdapter.RecycleItemClickListener {
    private static final String TAG = "BraveNewTabPageView";
    private static final String BRAVE_BINANCE = "https://brave.com/binance/";
    private static final String BRAVE_REF_URL = "https://brave.com/r/";

    private View mBraveStatsViewFallBackLayout;

    private ImageView bgImageView;
    private Profile mProfile;
    private BraveNewTabPageLayout ntpLayout;
    private BraveNewTabPageLayout ntpContent;
    private LinearLayout parentLayout;

    private SponsoredTab sponsoredTab;

    private BitmapDrawable imageDrawable;

    private FetchWallpaperWorkerTask mWorkerTask;
    private boolean isFromBottomSheet;
    private NTPBackgroundImagesBridge mNTPBackgroundImagesBridge;
    private ViewGroup mainLayout;
    private DatabaseHelper mDatabaseHelper;

    private ViewGroup mSiteSectionView;
    private TileGroup mTileGroup;
    private LottieAnimationView mBadgeAnimationView;
    private VerticalViewPager ntpWidgetViewPager;
    private NTPWidgetAdapter ntpWidgetAdapter;

    private Tab mTab;
    private Activity mActivity;
    private LinearLayout indicatorLayout;
    private LinearLayout superReferralSitesLayout;
    private LinearLayout ntpWidgetLayout;
    private LinearLayout bianceDisconnectLayout;
    private LinearLayout binanceWidgetLayout;
    private ProgressBar binanceWidgetProgress;
    private TextView mTopsiteErrorMessage;

    private BinanceNativeWorker mBinanceNativeWorker;
    private CryptoWidgetBottomSheetDialogFragment cryptoWidgetBottomSheetDialogFragment;
    private Timer countDownTimer;
    private List<NTPWidgetItem> widgetList = new ArrayList<NTPWidgetItem>();
    public static final int NTP_WIDGET_STACK_CODE = 3333;

    // Brave news
    private BraveNewsAdapter adapter;
    private FrameLayout optinButton;
    private TextView optinText;
    private LinearLayout optinLayout;
    private TextView optinLearnMore;
    private ImageView optinClose;
    private CopyOnWriteArrayList<NewsItem> newsItems = new CopyOnWriteArrayList<NewsItem>();
    private LinearLayout container;
    private RecyclerView recyclerView;
    private NestedScrollView nestedScrollView;
    private TextView loading;
    private View loadingView;
    private View feedSpinner;
    private ScrollView parentScrollView;
    private ViewGroup imageCreditLayout;
    private ViewGroup settingsBar;
    private boolean isScrolled;
    private NTPImage ntpImageGlobal;
    private boolean settingsBarIsClickable;

    private CompositorViewHolder compositorView;

    public BraveNewTabPageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProfile = Profile.getLastUsedRegularProfile();
        mNTPBackgroundImagesBridge = NTPBackgroundImagesBridge.getInstance(mProfile);
        mBinanceNativeWorker = BinanceNativeWorker.getInstance();
        mNTPBackgroundImagesBridge.setNewTabPageListener(newTabPageListener);
        mDatabaseHelper = DatabaseHelper.getInstance();
        Log.d("BN", "lifecycle init settingsBar3:" + settingsBar);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ntpWidgetLayout = findViewById(R.id.ntp_widget_layout);
        indicatorLayout = findViewById(R.id.indicator_layout);
        ntpWidgetViewPager = findViewById(R.id.ntp_widget_view_pager);
        ntpWidgetAdapter = new NTPWidgetAdapter();
        ntpWidgetAdapter.setNTPWidgetListener(ntpWidgetListener);
        ntpWidgetViewPager.setAdapter(ntpWidgetAdapter);
        Log.d("BN", "lifecycle init settingsBar4:" + settingsBar);
        ntpWidgetViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                cancelTimer();
                Log.d("BN", "lifecycle init settingsBar5:" + settingsBar);
                if (NTPWidgetManager.getInstance().getBinanceWidget() == position) {
                    startTimer();
                }
                updateAndShowIndicators(position);
                NTPWidgetManager.getInstance().setNTPWidgetOrder(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        showWidgetBasedOnOrder();
        NTPUtil.showBREBottomBanner(this);
    }

    private void showFallBackNTPLayout() {
        if (mBraveStatsViewFallBackLayout != null
                && mBraveStatsViewFallBackLayout.getParent() != null) {
            ((ViewGroup) mBraveStatsViewFallBackLayout.getParent())
                    .removeView(mBraveStatsViewFallBackLayout);
        }
        LayoutInflater inflater =
                (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBraveStatsViewFallBackLayout = inflater.inflate(R.layout.brave_stats_layout, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layoutParams.setMargins(0, dpToPx(mActivity, 16), 0, dpToPx(mActivity, 16));
        mBraveStatsViewFallBackLayout.setLayoutParams(layoutParams);
        mBraveStatsViewFallBackLayout.requestLayout();

        mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_title_layout)
                .setVisibility(View.GONE);
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_ads))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_data_saved_text))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_time))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(R.id.brave_stats_text_time_count))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        ((TextView) mBraveStatsViewFallBackLayout.findViewById(
                 R.id.brave_stats_text_time_count_text))
                .setTextColor(mActivity.getResources().getColor(R.color.shield_text_color));
        mBraveStatsViewFallBackLayout.setBackgroundColor(
                mActivity.getResources().getColor(android.R.color.transparent));
        mBraveStatsViewFallBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("SourceLockedOrientationActivity")
            public void onClick(View v) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                checkForBraveStats();
            }
        });
        BraveStatsUtil.updateBraveStatsLayout(mBraveStatsViewFallBackLayout);
        mainLayout.addView(mBraveStatsViewFallBackLayout, 0);

        int insertionPoint = mainLayout.indexOfChild(findViewById(R.id.ntp_middle_spacer)) + 1;
        if (mSiteSectionView.getParent() != null) {
            ((ViewGroup) mSiteSectionView.getParent()).removeView(mSiteSectionView);
        }
        mainLayout.addView(mSiteSectionView, insertionPoint);
    }

    protected void updateTileGridPlaceholderVisibility() {
        // This function is kept empty to avoid placeholder implementation
    }

    private List<NTPWidgetItem> setWidgetList() {
        NTPWidgetManager ntpWidgetManager = NTPWidgetManager.getInstance();
        LayoutInflater inflater =
                (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Map<Integer, NTPWidgetItem> ntpWidgetMap = new TreeMap<>();
        if (mSiteSectionView != null && mSiteSectionView.getParent() != null) {
            ((ViewGroup) mSiteSectionView.getParent()).removeView(mSiteSectionView);
        }

        for (String widget : ntpWidgetManager.getUsedWidgets()) {
            NTPWidgetItem ntpWidgetItem = NTPWidgetManager.mWidgetsMap.get(widget);
            if (widget.equals(NTPWidgetManager.PREF_PRIVATE_STATS)) {
                View mBraveStatsView = inflater.inflate(R.layout.brave_stats_layout, null);
                mBraveStatsView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @SuppressLint("SourceLockedOrientationActivity")
                    public void onClick(View v) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        checkForBraveStats();
                    }
                });
                ntpWidgetItem.setWidgetView(mBraveStatsView);
                ntpWidgetMap.put(ntpWidgetManager.getPrivateStatsWidget(), ntpWidgetItem);
            } else if (widget.equals(NTPWidgetManager.PREF_FAVORITES)) {
                View mTopSitesLayout = inflater.inflate(R.layout.top_sites_layout, null);
                FrameLayout mTopSitesGridLayout =
                        mTopSitesLayout.findViewById(R.id.top_sites_grid_layout);
                mTopsiteErrorMessage =
                        mTopSitesLayout.findViewById(R.id.widget_error_title);

                if (shouldShowSuperReferral() && superReferralSitesLayout != null) {
                    if (superReferralSitesLayout.getParent() != null) {
                        ((ViewGroup) superReferralSitesLayout.getParent())
                                .removeView(superReferralSitesLayout);
                    }
                    mTopSitesGridLayout.addView(superReferralSitesLayout);
                    ntpWidgetItem.setWidgetView(mTopSitesLayout);
                    ntpWidgetMap.put(ntpWidgetManager.getFavoritesWidget(), ntpWidgetItem);
                } else if (!mNTPBackgroundImagesBridge.isSuperReferral()
                        || !NTPBackgroundImagesBridge.enableSponsoredImages()
                        || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    boolean showPlaceholder = mTileGroup != null && mTileGroup.hasReceivedData()
                            && mTileGroup.isEmpty();
                    if (mSiteSectionView != null && !showPlaceholder) {
                        mTopsiteErrorMessage.setVisibility(View.GONE);
                        if (mSiteSectionView.getLayoutParams()
                                        instanceof ViewGroup.MarginLayoutParams) {
                            mSiteSectionView.setPadding(0, dpToPx(mActivity, 8), 0, 0);
                            mSiteSectionView.requestLayout();
                        }
                        mTopSitesGridLayout.addView(mSiteSectionView);
                    } else {
                        mTopsiteErrorMessage.setVisibility(View.VISIBLE);
                    }
                    ntpWidgetItem.setWidgetView(mTopSitesLayout);
                    ntpWidgetMap.put(ntpWidgetManager.getFavoritesWidget(), ntpWidgetItem);
                }
            } else if (widget.equals(NTPWidgetManager.PREF_BINANCE)) {
                View binanceWidgetView = inflater.inflate(R.layout.crypto_widget_layout, null);
                binanceWidgetLayout = binanceWidgetView.findViewById(R.id.binance_widget_layout);
                bianceDisconnectLayout =
                        binanceWidgetView.findViewById(R.id.binance_disconnect_layout);
                binanceWidgetProgress =
                        binanceWidgetView.findViewById(R.id.binance_widget_progress);
                binanceWidgetProgress.setVisibility(View.GONE);
                binanceWidgetView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (InternetConnection.isNetworkAvailable(mActivity)) {
                            if (BinanceWidgetManager.getInstance()
                                            .isUserAuthenticatedForBinance()) {
                                cancelTimer();
                                cryptoWidgetBottomSheetDialogFragment =
                                        new CryptoWidgetBottomSheetDialogFragment();
                                cryptoWidgetBottomSheetDialogFragment
                                        .setCryptoWidgetBottomSheetDialogDismissListener(
                                                BraveNewTabPageLayout.this);
                                cryptoWidgetBottomSheetDialogFragment.show(
                                        ((BraveActivity) mActivity).getSupportFragmentManager(),
                                        CryptoWidgetBottomSheetDialogFragment.TAG_FRAGMENT);
                            } else {
                                TabUtils.openUrlInSameTab(mBinanceNativeWorker.getOAuthClientUrl());
                                bianceDisconnectLayout.setVisibility(View.GONE);
                                binanceWidgetProgress.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(mActivity,
                                         mActivity.getResources().getString(
                                                 R.string.please_check_the_connection),
                                         Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
                Button connectButton = binanceWidgetView.findViewById(R.id.btn_connect);
                connectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TabUtils.openUrlInSameTab(mBinanceNativeWorker.getOAuthClientUrl());
                        bianceDisconnectLayout.setVisibility(View.GONE);
                        binanceWidgetProgress.setVisibility(View.VISIBLE);
                    }
                });
                ntpWidgetItem.setWidgetView(binanceWidgetView);
                ntpWidgetMap.put(ntpWidgetManager.getBinanceWidget(), ntpWidgetItem);
            }
        }

        return new ArrayList<NTPWidgetItem>(ntpWidgetMap.values());
    }

    private boolean shouldShowSuperReferral() {
        return mNTPBackgroundImagesBridge.isSuperReferral()
                && NTPBackgroundImagesBridge.enableSponsoredImages()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void showWidgetBasedOnOrder() {
        if (ntpWidgetViewPager != null) {
            int selectedOrder = NTPWidgetManager.getInstance().getNTPWidgetOrder();
            ntpWidgetViewPager.setCurrentItem(selectedOrder, true);
            updateAndShowIndicators(selectedOrder);
        }
    }

    private void showWidgets() {
        List<NTPWidgetItem> tempList = setWidgetList();
        if (tempList.size() > 0) {
            ntpWidgetLayout.setVisibility(View.VISIBLE);
            if (mBraveStatsViewFallBackLayout != null
                    && mBraveStatsViewFallBackLayout.getParent() != null) {
                ((ViewGroup) mBraveStatsViewFallBackLayout.getParent())
                        .removeView(mBraveStatsViewFallBackLayout);
            }
        } else {
            ntpWidgetLayout.setVisibility(View.GONE);
            if (!UserPrefs.get(Profile.getLastUsedRegularProfile())
                            .getBoolean(BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)) {
                showFallBackNTPLayout();
            }
        }

        if (ntpWidgetAdapter != null) {
            ntpWidgetAdapter.setWidgetList(tempList);
            ntpWidgetAdapter.notifyDataSetChanged();
            showWidgetBasedOnOrder();
        }
    }

    private void checkForBraveStats() {
        if (OnboardingPrefManager.getInstance().isBraveStatsEnabled()) {
            BraveStatsUtil.showBraveStats();
        } else {
            ((BraveActivity)mActivity).showOnboardingV2(true);
        }
    }

    protected void insertSiteSectionView() {
        mainLayout = findViewById(R.id.ntp_main_layout);

        mSiteSectionView = NewTabPageLayout.inflateSiteSection(mainLayout);
        ViewGroup.LayoutParams layoutParams = mSiteSectionView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        // If the explore sites section exists as its own section, then space it more closely.
        int variation = ExploreSitesBridge.getVariation();
        if (ExploreSitesBridge.isEnabled(variation)) {
            ((MarginLayoutParams) layoutParams).bottomMargin =
                getResources().getDimensionPixelOffset(
                    R.dimen.tile_grid_layout_vertical_spacing);
        }
        mSiteSectionView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        Log.d("BN", "lifecycle onAttachedToWindow");
        super.onAttachedToWindow();
        if (settingsBar != null) {
            Log.d("BN",
                    "lifecycle onAttachedToWindow settingsBar" + settingsBar.getAlpha()
                            + "setVisibility:" + settingsBar.getVisibility());
            settingsBar.setVisibility(View.VISIBLE);
            settingsBar.setAlpha(0f);
        }
        if (sponsoredTab == null) {
            initilizeSponsoredTab();
        }
        checkAndShowNTPImage(false);
        mNTPBackgroundImagesBridge.addObserver(mNTPBackgroundImageServiceObserver);
        if (PackageUtils.isFirstInstall(mActivity)
                && !OnboardingPrefManager.getInstance().isNewOnboardingShown()
                && OnboardingPrefManager.getInstance().isP3aOnboardingShown()) {
            ((BraveActivity)mActivity).showOnboardingV2(false);
        }
        if (OnboardingPrefManager.getInstance().isFromNotification() ) {
            ((BraveActivity)mActivity).showOnboardingV2(false);
            OnboardingPrefManager.getInstance().setFromNotification(false);
        }
        if (mBadgeAnimationView != null
                && !OnboardingPrefManager.getInstance().shouldShowBadgeAnimation()) {
            mBadgeAnimationView.setVisibility(View.INVISIBLE);
        }
        showWidgets();
        if (BinanceWidgetManager.getInstance().isUserAuthenticatedForBinance()) {
            if (binanceWidgetLayout != null) {
                binanceWidgetLayout.setVisibility(View.GONE);
            }
            mBinanceNativeWorker.getAccountBalances();
        }
        mBinanceNativeWorker.AddObserver(mBinanaceObserver);
        startTimer();
        initNews();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWorkerTask != null && mWorkerTask.getStatus() == AsyncTask.Status.RUNNING) {
            mWorkerTask.cancel(true);
            mWorkerTask = null;
        }

        if (!isFromBottomSheet) {
            setBackgroundResource(0);
            if (imageDrawable != null && imageDrawable.getBitmap() != null && !imageDrawable.getBitmap().isRecycled()) {
                imageDrawable.getBitmap().recycle();
            }
        }
        mNTPBackgroundImagesBridge.removeObserver(mNTPBackgroundImageServiceObserver);
        mBinanceNativeWorker.RemoveObserver(mBinanaceObserver);
        cancelTimer();
        Log.d("bn", "ondetach:" + settingsBar);
        if (settingsBar != null && compositorView != null) {
            // CompositorViewHolder compositorView = (CompositorViewHolder) rootView.getParent();
            if (settingsBar.getId() == R.id.news_settings_bar) {
                // compositorView.removeView(settingsBar);
                Log.d("bn", "ondetach b:" + settingsBar.getAlpha());
                settingsBar.setVisibility(View.GONE);
                settingsBar.setAlpha(0f);
                Log.d("bn", "ondetach a:" + settingsBar.getAlpha());
                // settingsBar.setVisibility(View.GONE);
                // compositorView.removeView(settingsBar);
            }
        } else {
            Log.d("bn", "ondetach null:" + settingsBar);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("BN", "onConfigurationChanged");
        if (sponsoredTab != null && NTPUtil.shouldEnableNTPFeature()) {
            if (bgImageView != null) {
                Log.d("BN", "onConfigurationChanged bgimage set");
                // We need to redraw image to fit parent properly
                // bgImageView.setImageResource(android.R.color.transparent);
            }
            NTPImage ntpImage = sponsoredTab.getTabNTPImage(false);
            if (ntpImage == null) {
                sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
            } else if (ntpImage instanceof Wallpaper) {
                Wallpaper mWallpaper = (Wallpaper) ntpImage;
                if (mWallpaper == null) {
                    sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
                }
            }
            checkForNonDisruptiveBanner(ntpImage);
            super.onConfigurationChanged(newConfig);
            showNTPImage(ntpImage);
        } else {
            super.onConfigurationChanged(newConfig);
        }
    }

    // public int correctImageCreditLayoutTopPosition(){
    //     DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    //     float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
    //     int pxHeight = dpToPx(mActivity, dpHeight);

    //     boolean isTablet = ConfigurationUtils.isTablet(mActivity);
    //     boolean isLandscape = ConfigurationUtils.isLandscape(mActivity);
    //     int imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.46) : (int) (pxHeight *
    //     0.70); if (ntpImageGlobal instanceof BackgroundImage) {
    //         if (!isTablet ){
    //             // imageCreditCorrection = isLandscape ? (int) (dpHeight - 250) : (int) (dpHeight
    //             + 150); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.12) : (int)
    //             (pxHeight * 0.45);
    //         }
    //     } else {
    //         if (!isTablet ){
    //             Log.d("bn", "phone sponsored image dpHeight:"+dpHeight);
    //             // imageCreditCorrection = isLandscape ? (int) (dpHeight - 350) : (int) (dpHeight
    //             - 120); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.02) : (int)
    //             (pxHeight * 0.30);
    //         } else {
    //             Log.d("bn", "tablet sponsored image dpHeight:"+dpHeight);
    //             // imageCreditCorrection = isLandscape ? (int) (dpHeight - 320) : (int) (dpHeight
    //             + 150); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.28) : (int)
    //             (pxHeight * 0.56);

    //         }
    //     }
    // }

    // corrects position of image credit and for the loading spinner. Used when News is active
    private void correctPosition() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        int pxHeight = dpToPx(mActivity, dpHeight);

        boolean isTablet = ConfigurationUtils.isTablet(mActivity);
        boolean isLandscape = ConfigurationUtils.isLandscape(mActivity);

        // LinearLayout.LayoutParams linearLayoutParams = new
        // LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        // LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams linearLayoutParams =
                (LinearLayout.LayoutParams) imageCreditLayout.getLayoutParams();
        FrameLayout.LayoutParams feedSpinnerParams =
                (FrameLayout.LayoutParams) feedSpinner.getLayoutParams();
        FrameLayout bottomToolbar = (FrameLayout) findViewById(R.id.bottom_toolbar);
        Log.d("bn",
                "onload - changing the layout positions before top marign :"
                        + linearLayoutParams.topMargin
                        + " feedSpinnerParams.topMargin:" + feedSpinnerParams.topMargin);
        // int imageCreditCorrection = isTablet ? isLandscape ? (int) (0.20 * mDeviceWidth) : (int)
        // (0.10 * mDeviceWidth) : 40; int imageCreditCorrection = isLandscape ? (int) (dpHeight -
        // 50) : (int) (dpHeight + 450);

        Log.d("bn",
                "onload - changing the layout positions before top marign :"
                        + linearLayoutParams.topMargin
                        + " feedSpinnerParams.topMargin:" + feedSpinnerParams.topMargin);

        Log.d("bn", "margintop dpHeight:" + dpHeight);
        Log.d("bn", "margintop dptopx dpHeight:" + dpToPx(mActivity, dpHeight));
        Log.d("bn", "margintop displayMetrics.heightPixels:" + displayMetrics.heightPixels);
        Log.d("bn", "margintop bottomToolbar:" + bottomToolbar);
        // Log.d("bn", "margintop bottomToolbar:" + bottomToolbar.getTop());
        Log.d("bn",
                "margintop displayMetrics.heightPixels dpToPx:"
                        + dpToPx(mActivity, displayMetrics.heightPixels));
        Log.d("bn", "margintop 150 dpToPx:" + dpToPx(mActivity, 150));
        Log.d("bn", "margintop 250 dpToPx:" + dpToPx(mActivity, 250));
        Log.d("bn", "margintop 350 dpToPx:" + dpToPx(mActivity, 350));
        // int imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.46) : (int) (pxHeight *
        // 0.70); if (ntpImageGlobal instanceof BackgroundImage) {
        //     if (!isTablet ){
        //         // imageCreditCorrection = isLandscape ? (int) (dpHeight - 250) : (int) (dpHeight
        //         + 150); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.12) : (int)
        //         (pxHeight * 0.45);
        //     }
        // } else {
        //     if (!isTablet ){
        //         Log.d("bn", "phone sponsored image dpHeight:"+dpHeight);
        //         // imageCreditCorrection = isLandscape ? (int) (dpHeight - 350) : (int) (dpHeight
        //         - 120); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.02) : (int)
        //         (pxHeight * 0.30);
        //     } else {
        //         Log.d("bn", "tablet sponsored image dpHeight:"+dpHeight);
        //         // imageCreditCorrection = isLandscape ? (int) (dpHeight - 320) : (int) (dpHeight
        //         + 150); imageCreditCorrection = isLandscape ? (int) (pxHeight * 0.28) : (int)
        //         (pxHeight * 0.56);

        //     }
        // }

        int imageCreditCorrection = NTPUtil.correctImageCreditLayoutTopPosition(ntpImageGlobal);

        linearLayoutParams.setMargins(0, imageCreditCorrection, 0, 0);
        Log.d("bn", "margintop imageCreditCorrection applied:" + imageCreditCorrection);
        Log.d("bn", "margintop imageCreditCorrection:" + imageCreditLayout.getTop());
        // Log.d("bn", "margintop imageCreditCorrection:"+imageCreditLayout.getTop());
        imageCreditLayout.setLayoutParams(linearLayoutParams);
        int imageCreditLayoutBottom = imageCreditLayout.getBottom();
        feedSpinnerParams.topMargin = (int) (dpHeight
                - 50); // imageCreditLayoutBottom;// imageCreditLayout.getBottom() + 20;
        feedSpinner.setLayoutParams(feedSpinnerParams);
    }

    private void initNews() {
        Log.d("bravenews", "in initNews");
        settingsBarIsClickable = false;
        recyclerView = findViewById(R.id.newsRecycler);
        container = (LinearLayout) findViewById(R.id.ntp_main_layout);
        // nestedScrollView = findViewById(R.id.nestedScrollView);
        optinButton = findViewById(R.id.optin_button);
        optinClose = findViewById(R.id.close_optin);
        optinLearnMore = findViewById(R.id.optin_learnmore);
        optinLayout = findViewById(R.id.optin_layout_id);
        optinText = findViewById(R.id.optin_button_text);
        loading = findViewById(R.id.loading);
        loadingView = findViewById(R.id.optin_loading_spinner);
        feedSpinner = findViewById(R.id.feed_spinner);
        parentLayout = (LinearLayout) findViewById(R.id.parent_layout);
        ntpContent = (BraveNewTabPageLayout) findViewById(R.id.ntp_content);

        recyclerView.setItemViewCacheSize(20);
        // recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        recyclerView.setVisibility(View.GONE);
        // recyclerView.setScrollContainer(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        adapter = new BraveNewsAdapter(mActivity, newsItems);
        recyclerView.setAdapter(adapter);
        // adapter.setClickListener(this);
        loadingView.setVisibility(View.GONE);

        parentScrollView = (ScrollView) ntpContent.getParent();
        ViewGroup rootView = (ViewGroup) parentScrollView.getParent();
        compositorView = (CompositorViewHolder) rootView.getParent();

        imageCreditLayout = findViewById(R.id.image_credit_layout);

        optinLayout.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = ContextUtils.getAppSharedPreferences();
        boolean isNewsOn =
                sharedPreferences.getBoolean(BraveNewsPreferences.PREF_TURN_ON_NEWS, false);
        boolean isShowOptin =
                sharedPreferences.getBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, true);
        boolean isShowNewsOn =
                sharedPreferences.getBoolean(BraveNewsPreferences.PREF_SHOW_NEWS, false);

        Log.d("bn", "layout optin isNewsOn:" + isNewsOn + " isShowNewsOn:" + isShowNewsOn);
        if (!isNewsOn && isShowOptin) {
            optinLayout.setVisibility(View.VISIBLE);
        } else if (isShowNewsOn) {
            correctPosition();

            optinLayout.setVisibility(View.GONE);
            parentLayout.removeView(optinLayout);
            feedSpinner.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("bravenews", "getting feed...");

                    getFeed();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("BN", "optin click after");

                            feedSpinner.setVisibility(View.GONE);
                            optinLayout.setVisibility(View.GONE);

                            container.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);

                            // nestedScrollView.setVisibility(View.VISIBLE);
                            // preferences.setOptIn(true);

                            adapter.notifyDataSetChanged();

                            parentScrollView.scrollTo(0, 0);
                            // parentScrollView.fullScroll(ScrollView.FOCUS_UP);

                            isScrolled = true;
                            Log.d("BN", "optin click after recycler y0:" + recyclerView.getY());

                            if (recyclerView.getLayoutManager().findViewByPosition(0) != null) {
                                Log.d("BN",
                                        "optin click after y0:"
                                                + recyclerView.getLayoutManager()
                                                          .findViewByPosition(0)
                                                          .getY());
                            }
                        }
                    }, 1000);
                }
            }).start();
        }

        ViewTreeObserver parentScrollViewObserver = parentScrollView.getViewTreeObserver();
        parentScrollView.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        int scrollY = parentScrollView.getScrollY();

                        if (recyclerView.getLayoutManager().findViewByPosition(0) != null) {
                            if (isScrolled) {
                                correctPosition();
                            }
                        }

                        isScrolled = false;

                        float onScrollChangedValue = parentScrollView.getMaxScrollAmount();
                        // Log.d("BN", "onScrollChangedValue view.scrollY():" + scrollY);
                        // Log.d("BN", "onScrollChangedValue view.getMaxScrollAmount():" +
                        // onScrollChangedValue);
                        float value = (float) scrollY / parentScrollView.getMaxScrollAmount();
                        if (value >= 1) {
                            value = 1;
                        }
                        float alpha = (float) (1 - value);
                        if (alpha < 1f) {
                            imageCreditLayout.setAlpha(alpha);
                        }

                        Log.d("bn",
                                "parentScrollViewObserver is alive:"
                                        + parentScrollViewObserver.isAlive());
                        if (settingsBar != null && settingsBar.getVisibility() == View.VISIBLE) {
                            if (value > 0.4 && settingsBar.getAlpha() <= 1f) {
                                settingsBar.setAlpha((float) (value + 0.5));
                            } else if (value < 0.4 && settingsBar.getAlpha() > 0f) {
                                settingsBar.setAlpha((float) (value - 0.2));
                            }
                            if (settingsBar.getAlpha() >= 1) {
                                settingsBarIsClickable = true;
                            } else {
                                settingsBarIsClickable = false;
                                // settingsBar.setVisibility(View.GONE);
                            }
                            Log.d("BN",
                                    "settings bar alpha: " + settingsBar.getAlpha()
                                            + "isClickable:" + settingsBarIsClickable);
                        }
                    }
                });

        parentScrollViewObserver.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d("BN",
                                "settings bar on onGlobalLayout: " + settingsBar
                                        + " isScrolled:" + isScrolled);
                        // if (BraveActivity.getBraveActivity() != null
                        //         && BraveActivity.getBraveActivity().getActivityTab() != null) {
                        //     if
                        //     (UrlUtilities.isNTPUrl(BraveActivity.getBraveActivity().getActivityTab().getUrlString())){
                        //         Log.d("BN", "settings bar on onGlobalLayout is ntp:
                        //         "+settingsBar);
                        //     } else {
                        //         Log.d("BN", "settings bar on onGlobalLayout is not ntp:
                        //         "+settingsBar);
                        //         BraveActivity.getBraveActivity().removeSetttingsBar();
                        //     }
                        //     Log.d("BN", "settings bar on onGlobalLayout is nothing ntp:
                        //     "+settingsBar);
                        // } else {
                        //     Log.d("BN", "settings bar on onGlobalLayout is null");
                        // }
                        if (settingsBar != null) {
                            if (!isScrolled) {
                                settingsBar.setAlpha(0f);
                            }

                            Log.d("BN",
                                    "settings bar on onGlobalLayout: " + settingsBar.getAlpha());
                            settingsBar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d("bn",
                                            "clicked on settings bar 1:" + settingsBar.getAlpha()
                                                    + " isClickable:" + settingsBarIsClickable);
                                }
                            });
                            ImageView newsSettingsButton =
                                    (ImageView) settingsBar.findViewById(R.id.news_settings_button);
                            ViewTreeObserver.OnGlobalLayoutListener listener = this;
                            Log.d("bn",
                                    "parentScrollViewObserver is alive before:"
                                            + parentScrollViewObserver.isAlive());

                            newsSettingsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (settingsBarIsClickable) {
                                        SettingsLauncher settingsLauncher =
                                                new SettingsLauncherImpl();
                                        settingsLauncher.launchSettingsActivity(
                                                getContext(), BraveNewsPreferences.class);
                                        parentScrollView.getViewTreeObserver()
                                                .removeOnGlobalLayoutListener(listener);
                                    }
                                }
                            });
                            Log.d("bn",
                                    "parentScrollViewObserver is alive after:"
                                            + parentScrollViewObserver.isAlive());
                            // }
                        } else {
                            Log.d("BN", "settings bar on onGlobalLayout is null: " + settingsBar);
                        }
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int offset = recyclerView.computeVerticalScrollOffset();

                Log.d("BN", "onScrollStateChanged:" + newState + " offset:" + offset);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int offset = recyclerView.computeVerticalScrollOffset();
                Log.d("BN", "onScrolled dx:" + dx + " dy:" + dy + " offset:" + offset);
                parentScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("BN",
                                "onScrolled scroll to:" + offset + " positionL:"
                                        + parentScrollView.getVerticalScrollbarPosition());

                        parentScrollView.scrollBy(0, offset + 50);
                    }
                });
            }
        });

        optinClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BN", "close_optin");

                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
                sharedPreferencesEditor.apply();
                if (optinLayout != null) {
                    optinLayout.setVisibility(View.GONE);
                }
                correctPosition();
                // if (parentScrollView != null){
                parentScrollView.scrollTo(0, 0);
                imageCreditLayout.setAlpha(1.0f);
                // }
            }
        });

        optinLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BN", "optin_learnmorex");
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                mActivity.startActivity(browserIntent);
            }
        });

        optinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BN", "optin click");

                loadingView.setVisibility(View.VISIBLE);
                optinButton.setClickable(false);
                optinText.setVisibility(View.INVISIBLE);
                loadingView.setVisibility(View.VISIBLE);

                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_TURN_ON_NEWS, true);
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_SHOW_NEWS, true);
                sharedPreferencesEditor.putBoolean(BraveNewsPreferences.PREF_SHOW_OPTIN, false);
                sharedPreferencesEditor.apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("bravenews", "getting feed...");
                        getFeed();
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("BN", "optin click after");

                                loadingView.setVisibility(View.GONE);
                                optinLayout.setVisibility(View.GONE);
                                parentLayout.removeView(optinLayout);

                                container.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);

                                // nestedScrollView.setVisibility(View.VISIBLE);
                                // preferences.setOptIn(true);

                                adapter.notifyDataSetChanged();

                                parentScrollView.scrollTo(0, 0);
                                // parentScrollView.fullScroll(ScrollView.FOCUS_UP);

                                isScrolled = true;
                                Log.d("BN", "optin click after recycler y0:" + recyclerView.getY());

                                if (recyclerView.getLayoutManager().findViewByPosition(0) != null) {
                                    Log.d("BN",
                                            "optin click after y0:"
                                                    + recyclerView.getLayoutManager()
                                                              .findViewByPosition(0)
                                                              .getY());
                                }
                            }
                        }, 1000);
                    }
                }).start();
            }
        });
    }

    private void getFeed() {
        BraveNewsUtils utils = new BraveNewsUtils(mActivity);
        newsItems = utils.parseJson(newsItems);
        Log.d("bravenews", "end parse");
    }

    @Override
    public void initialize(NewTabPageManager manager, Activity activity,
            TileGroup.Delegate tileGroupDelegate, boolean searchProviderHasLogo,
            boolean searchProviderIsGoogle, ScrollDelegate scrollDelegate,
            ContextMenuManager contextMenuManager, UiConfig uiConfig, Supplier<Tab> tabProvider,
            ActivityLifecycleDispatcher lifecycleDispatcher, NewTabPageUma uma, boolean isIncognito,
            WindowAndroid windowAndroid) {
        super.initialize(manager, activity, tileGroupDelegate, searchProviderHasLogo,
                searchProviderIsGoogle, scrollDelegate, contextMenuManager, uiConfig, tabProvider,
                lifecycleDispatcher, uma, isIncognito, windowAndroid);

        assert (activity instanceof BraveActivity);
        mActivity = activity;
        ((BraveActivity) mActivity).dismissShieldsTooltip();
        CompositorViewHolder compositorView = findViewById(R.id.compositor_view_holder);
        Log.d("BN", "lifecycle init compositorview:" + compositorView);
        Log.d("BN", "lifecycle init settingsBar:" + settingsBar);
    }

    private void showNTPImage(NTPImage ntpImage) {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        NTPUtil.updateOrientedUI(mActivity, this, size, ntpImage);
        ImageView mSponsoredLogo = (ImageView) findViewById(R.id.sponsored_logo);
        FloatingActionButton mSuperReferralLogo = (FloatingActionButton) findViewById(R.id.super_referral_logo);
        TextView mCreditText = (TextView) findViewById(R.id.credit_text);
        LinearLayout optinLayout = (LinearLayout) findViewById(R.id.optin_layout_id);
        ntpImageGlobal = ntpImage;
        if (ntpImage instanceof Wallpaper
                && NTPUtil.isReferralEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("BN", "bgimage if");
            Log.d("bn", "bgimagestep 0");
            setBackgroundImage(ntpImage);
            mSuperReferralLogo.setVisibility(View.VISIBLE);
            mCreditText.setVisibility(View.GONE);
            int floatingButtonIcon = R.drawable.ic_qr_code;
            mSuperReferralLogo.setImageResource(floatingButtonIcon);
            int floatingButtonIconColor =
                    GlobalNightModeStateProviderHolder.getInstance().isInNightMode()
                    ? android.R.color.white
                    : android.R.color.black;
            ImageViewCompat.setImageTintList(
                    mSuperReferralLogo, ColorStateList.valueOf(floatingButtonIconColor));
            mSuperReferralLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    QRCodeShareDialogFragment mQRCodeShareDialogFragment =
                            new QRCodeShareDialogFragment();
                    mQRCodeShareDialogFragment.setQRCodeText(
                            BRAVE_REF_URL + mNTPBackgroundImagesBridge.getSuperReferralCode());
                    mQRCodeShareDialogFragment.show(
                            ((BraveActivity) mActivity).getSupportFragmentManager(),
                            "QRCodeShareDialogFragment");
                }
            });
        } else if (UserPrefs.get(Profile.getLastUsedRegularProfile()).getBoolean(
                       BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)
                   && sponsoredTab != null
                   && NTPUtil.shouldEnableNTPFeature()) {
            Log.d("BN", "bgimage else if");
            setBackgroundImage(ntpImage);
            // optinLayout.setVisibility(View.VISIBLE);
            if (ntpImage instanceof BackgroundImage) {
                BackgroundImage backgroundImage = (BackgroundImage) ntpImage;
                mSponsoredLogo.setVisibility(View.GONE);
                mSuperReferralLogo.setVisibility(View.GONE);

                if (backgroundImage.getImageCredit() != null) {
                    Log.d("bn", "bgimagestep 1");
                    String imageCreditStr = String.format(getResources().getString(R.string.photo_by, backgroundImage.getImageCredit().getName()));

                    SpannableStringBuilder spannableString = new SpannableStringBuilder(imageCreditStr);
                    spannableString.setSpan(
                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        ((imageCreditStr.length() - 1)
                         - (backgroundImage.getImageCredit().getName().length() - 1)),
                        imageCreditStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mCreditText.setText(spannableString);
                    mCreditText.setVisibility(View.VISIBLE);

                    mCreditText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (backgroundImage.getImageCredit() != null) {
                                TabUtils.openUrlInSameTab(
                                        backgroundImage.getImageCredit().getUrl());
                            }
                        }
                    });
                } else {
                    Log.d("bn", "bgimagestep 2");
                }
            } else {
                Log.d("bn", "bgimagestep 3");
            }
        } else {
            Log.d("BN", "bgimage else");
        }
    }

    private void setBackgroundImage(NTPImage ntpImage) {
        bgImageView = (ImageView) findViewById(R.id.bg_image_view);
        // ntpLayout = (BraveNewTabPageLayout) findViewById(R.id.ntp_content);
        // bgImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Log.d("BN", "setting bgImageView:" + ntpImage);

        ViewTreeObserver observer = bgImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mWorkerTask = new FetchWallpaperWorkerTask(ntpImage, bgImageView.getMeasuredWidth(),
                        bgImageView.getMeasuredHeight(), wallpaperRetrievedCallback);
                mWorkerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                bgImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void checkForNonDisruptiveBanner(NTPImage ntpImage) {
        int brOption = NTPUtil.checkForNonDisruptiveBanner(ntpImage, sponsoredTab);
        if (SponsoredImageUtil.BR_INVALID_OPTION != brOption && !NTPUtil.isReferralEnabled()
                && ((!BraveAdsNativeHelper.nativeIsBraveAdsEnabled(
                             Profile.getLastUsedRegularProfile())
                            && BraveRewardsHelper.shouldShowBraveRewardsOnboardingModal())
                        || BraveAdsNativeHelper.nativeIsBraveAdsEnabled(
                                Profile.getLastUsedRegularProfile()))) {
            NTPUtil.showNonDisruptiveBanner((BraveActivity) mActivity, this, brOption,
                                             sponsoredTab, newTabPageListener);
        }
    }

    private void checkAndShowNTPImage(boolean isReset) {
        NTPImage ntpImage = sponsoredTab.getTabNTPImage(isReset);
        Log.d("BN", "checking NTP");
        if (ntpImage == null) {
            Log.d("BN", "ntpImage null setting 1");
            sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
        } else if (ntpImage instanceof Wallpaper) {
            Log.d("BN", "ntpImage not null setting 2");

            Wallpaper mWallpaper = (Wallpaper) ntpImage;
            if (mWallpaper == null) {
                sponsoredTab.setNTPImage(SponsoredImageUtil.getBackgroundImage());
            }
        }
        checkForNonDisruptiveBanner(ntpImage);
        showNTPImage(ntpImage);
    }

    private void initilizeSponsoredTab() {
        if (TabAttributes.from(getTab()).get(String.valueOf(getTabImpl().getId())) == null) {
            SponsoredTab mSponsoredTab = new SponsoredTab(mNTPBackgroundImagesBridge);
            TabAttributes.from(getTab()).set(String.valueOf(getTabImpl().getId()), mSponsoredTab);
        }
        sponsoredTab = TabAttributes.from(getTab()).get(String.valueOf((getTabImpl()).getId()));
        if (shouldShowSuperReferral()) mNTPBackgroundImagesBridge.getTopSites();
    }

    private NewTabPageListener newTabPageListener = new NewTabPageListener() {
        @Override
        public void updateInteractableFlag(boolean isBottomSheet) {
            isFromBottomSheet = isBottomSheet;
        }

        @Override
        public void updateNTPImage() {
            if (sponsoredTab == null) {
                initilizeSponsoredTab();
            }
            checkAndShowNTPImage(false);

            Log.d("bn", "updateNTPImage");
            parentScrollView = (ScrollView) ntpContent.getParent();
            Log.d("bn", "rootView:" + parentScrollView);
            ViewGroup rootView = (ViewGroup) parentScrollView.getParent();
            Log.d("bn", "rootView:" + rootView);
            CompositorViewHolder compositorView = (CompositorViewHolder) rootView.getParent();
            final int childCount = compositorView.getChildCount();
            Log.d("bn", "childCount:" + childCount);
            for (int i = 0; i < childCount; i++) {
                View v = compositorView.getChildAt(i);
                Log.d("bn", "child view :" + v);
            }
        }

        @Override
        public void updateTopSites(List<TopSite> topSites) {
            new AsyncTask<List<TopSiteTable>>() {
                @Override
                protected List<TopSiteTable> doInBackground() {
                    for (TopSite topSite : topSites) {
                        mDatabaseHelper.insertTopSite(topSite);
                    }
                    return mDatabaseHelper.getAllTopSites();
                }

                @Override
                protected void onPostExecute(List<TopSiteTable> topSites) {
                    assert ThreadUtils.runningOnUiThread();
                    if (isCancelled()) return;

                    loadTopSites(topSites);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    };

    private NTPBackgroundImagesBridge.NTPBackgroundImageServiceObserver mNTPBackgroundImageServiceObserver = new NTPBackgroundImagesBridge.NTPBackgroundImageServiceObserver() {
        @Override
        public void onUpdated() {
            if (NTPUtil.isReferralEnabled()) {
                checkAndShowNTPImage(true);
                if (shouldShowSuperReferral()) {
                    mNTPBackgroundImagesBridge.getTopSites();
                }
            }
        } 
    };

    private FetchWallpaperWorkerTask.WallpaperRetrievedCallback wallpaperRetrievedCallback = new FetchWallpaperWorkerTask.WallpaperRetrievedCallback() {
        @Override
        public void bgWallpaperRetrieved(Bitmap bgWallpaper) {
            Log.d("BN", "setting wallpaper:" + bgWallpaper);
            Log.d("BN", "lifecycle init settingsBar2:" + settingsBar);
            if (BraveActivity.getBraveActivity() != null) {
                BraveActivity.getBraveActivity().setBackground(bgWallpaper);
            }

            parentScrollView = (ScrollView) ntpContent.getParent();
            Log.d("bn", "rootView:" + parentScrollView);
            ViewGroup rootView = (ViewGroup) parentScrollView.getParent();
            Log.d("bn", "rootView:" + rootView);
            ViewGroup coordinator = (ViewGroup) rootView.getParent();
            ViewStub bottomControlsStub = (ViewStub) findViewById(R.id.bottom_controls_stub);
            if (bottomControlsStub != null) {
                Log.d("bn", "margintop callback bottomControlsStub:" + bottomControlsStub.getTop());
            }
            if (coordinator != null) {
                ViewGroup bottomBar = (ViewGroup) rootView.findViewById(R.id.bottom_controls);
                if (bottomBar != null) {
                    Log.d("bn", "margintop callback bottomBar:" + bottomBar.getTop());
                }
            }
            CompositorViewHolder compositorView = (CompositorViewHolder) rootView.getParent();
            Log.d("bn", "compositorView:" + compositorView);

            final int childCount = compositorView.getChildCount();
            Log.d("bn", "childCount:" + childCount);
            for (int index = 0; index < ((ViewGroup) compositorView).getChildCount(); index++) {
                View nextChild = ((ViewGroup) compositorView).getChildAt(index);
                Log.d("bn", "compositorViewchildren ntplayout nextchild before add:" + nextChild);
            }

            // @TODO type check and null checks for all views added by getChild or similar
            try {
                if (UrlUtilities.isNTPUrl(
                            BraveActivity.getBraveActivity().getActivityTab().getUrlString())) {
                    if (compositorView.getChildAt(2).getId() == R.id.news_settings_bar) {
                        settingsBar = (LinearLayout) compositorView.getChildAt(2);
                        Log.d("bn", "fetched settings bar :" + settingsBar.getId());
                        Log.d("bn", "fetched settings bar :" + R.id.news_settings_bar);
                        Log.d("bn",
                                "fetched settings bar :" + R.layout.brave_news_settings_bar_layout);
                        settingsBar.setAlpha(0f);
                    }
                }
            } catch (Exception e) {
                Log.e("bn", "fetched settings bar: " + e.getMessage());
            }
        }

        @Override
        public void logoRetrieved(Wallpaper mWallpaper, Bitmap logoWallpaper) {
            if (!NTPUtil.isReferralEnabled()) {
                FloatingActionButton mSuperReferralLogo = (FloatingActionButton) findViewById(R.id.super_referral_logo);
                mSuperReferralLogo.setVisibility(View.GONE);

                ImageView sponsoredLogo = (ImageView) findViewById(R.id.sponsored_logo);
                sponsoredLogo.setVisibility(View.VISIBLE);
                sponsoredLogo.setImageBitmap(logoWallpaper);
                sponsoredLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mWallpaper.getLogoDestinationUrl() != null) {
                            TabUtils.openUrlInSameTab(mWallpaper.getLogoDestinationUrl());
                            mNTPBackgroundImagesBridge.wallpaperLogoClicked(mWallpaper);
                        }
                    }
                });
            }
        }
    };

    private void loadTopSites(List<TopSiteTable> topSites) {
        superReferralSitesLayout = new LinearLayout(mActivity);
        superReferralSitesLayout.setWeightSum(1f);
        superReferralSitesLayout.setOrientation(LinearLayout.HORIZONTAL);
        superReferralSitesLayout.setBackgroundColor(
                mActivity.getResources().getColor(R.color.topsite_bg_color));

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (TopSiteTable topSite : topSites) {
            final View view = inflater.inflate(R.layout.suggestions_tile_view, null);

            TextView tileViewTitleTv = view.findViewById(R.id.tile_view_title);
            tileViewTitleTv.setText(topSite.getName());
            tileViewTitleTv.setTextColor(getResources().getColor(android.R.color.black));

            ImageView iconIv = view.findViewById(R.id.tile_view_icon);
            if (NTPUtil.imageCache.get(topSite.getDestinationUrl()) == null) {
                NTPUtil.imageCache.put(topSite.getDestinationUrl(), new java.lang.ref.SoftReference(NTPUtil.getTopSiteBitmap(topSite.getImagePath())));
            }
            iconIv.setImageBitmap(NTPUtil.imageCache.get(topSite.getDestinationUrl()).get());
            iconIv.setBackgroundColor(mActivity.getResources().getColor(android.R.color.white));
            iconIv.setClickable(false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TabUtils.openUrlInSameTab(topSite.getDestinationUrl());
                }
            });

            view.setPadding(0, dpToPx(mActivity, 12), 0, 0);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 0.25f;
            layoutParams.gravity = Gravity.CENTER;
            view.setLayoutParams(layoutParams);
            view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add(R.string.contextmenu_open_in_new_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TabUtils.openUrlInNewTab(false, topSite.getDestinationUrl());
                            return true;
                        }
                    });
                    menu.add(R.string.contextmenu_open_in_incognito_tab).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            TabUtils.openUrlInNewTab(true, topSite.getDestinationUrl());
                            return true;
                        }
                    });
                    menu.add(R.string.contextmenu_save_link).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (getTab() != null) {
                                OfflinePageBridge.getForProfile(mProfile).scheduleDownload(getTab().getWebContents(),
                                        OfflinePageBridge.NTP_SUGGESTIONS_NAMESPACE, topSite.getDestinationUrl(), DownloadUiActionFlags.ALL);
                            } else {
                                RequestCoordinatorBridge.getForProfile(mProfile).savePageLater(
                                    topSite.getDestinationUrl(), OfflinePageBridge.NTP_SUGGESTIONS_NAMESPACE, true /* userRequested */);
                            }
                            return true;
                        }
                    });
                    menu.add(R.string.remove).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            NTPUtil.imageCache.remove(topSite.getDestinationUrl());
                            mDatabaseHelper.deleteTopSite(topSite.getDestinationUrl());
                            NTPUtil.addToRemovedTopSite(topSite.getDestinationUrl());
                            superReferralSitesLayout.removeView(view);
                            return true;
                        }
                    });
                }
            });
            superReferralSitesLayout.addView(view);
        }
        showWidgets();
    }

    public void setTab(Tab tab) {
        mTab = tab;
    }

    private Tab getTab() {
        assert mTab != null;
        return mTab;
    }

    private TabImpl getTabImpl() {
        return (TabImpl) getTab();
    }

    private void updateAndShowIndicators(int position) {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < ntpWidgetAdapter.getCount(); i++) {
            TextView dotTextView = new TextView(mActivity);
            dotTextView.setText(Html.fromHtml("&#9679;"));
            dotTextView.setTextColor(getResources().getColor(android.R.color.white));
            dotTextView.setTextSize(8);
            if (position == i) {
                dotTextView.setAlpha(1.0f);
            } else {
                dotTextView.setAlpha(0.4f);
            }
            indicatorLayout.addView(dotTextView);
        }
    }

    // NTP related methods
    private NTPWidgetAdapter.NTPWidgetListener ntpWidgetListener =
            new NTPWidgetAdapter.NTPWidgetListener() {
                @Override
                public void onMenuEdit() {
                    cancelTimer();
                    openWidgetStack();
                }

                @Override
                public void onMenuRemove(int position, boolean isBinanceWidget) {
                    if (isBinanceWidget) {
                        mBinanceNativeWorker.revokeToken();
                        BinanceWidgetManager.getInstance().setBinanceAccountBalance("");
                        BinanceWidgetManager.getInstance().setUserAuthenticationForBinance(false);
                        if (cryptoWidgetBottomSheetDialogFragment != null) {
                            cryptoWidgetBottomSheetDialogFragment.dismiss();
                        }
                    }

                    if (BraveActivity.getBraveActivity() != null 
                        && BraveActivity.getBraveActivity().getActivityTab() != null 
                        && !UserPrefs.get(Profile.getLastUsedRegularProfile())
                            .getBoolean(BravePref.NEW_TAB_PAGE_SHOW_BACKGROUND_IMAGE)
                        && NTPWidgetManager.getInstance().getUsedWidgets().size() <= 0) {
                        BraveActivity.getBraveActivity().getActivityTab().reloadIgnoringCache();
                    } else {
                        showWidgets();
                    }
                }

                @Override
                public void onMenuLearnMore() {
                    TabUtils.openUrlInSameTab(BRAVE_BINANCE);
                }

                @Override
                public void onMenuRefreshData() {
                    mBinanceNativeWorker.getAccountBalances();
                }

                @Override
                public void onMenuDisconnect() {
                    mBinanceNativeWorker.revokeToken();
                    BinanceWidgetManager.getInstance().setBinanceAccountBalance("");
                    BinanceWidgetManager.getInstance().setUserAuthenticationForBinance(false);
                    if (cryptoWidgetBottomSheetDialogFragment != null) {
                        cryptoWidgetBottomSheetDialogFragment.dismiss();
                    }
                    // Reset binance widget to connect page
                    showWidgets();
                }
            };

    private BinanceObserver mBinanaceObserver = new BinanceObserver() {
        @Override
        public void OnGetAccessToken(boolean isSuccess) {
            BinanceWidgetManager.getInstance().setUserAuthenticationForBinance(isSuccess);
            if (isSuccess) {
                mBinanceNativeWorker.getAccountBalances();
                if (bianceDisconnectLayout != null) {
                    bianceDisconnectLayout.setVisibility(View.GONE);
                }
                if (binanceWidgetProgress != null) {
                    binanceWidgetProgress.setVisibility(View.VISIBLE);
                }
            }
        };

        @Override
        public void OnGetAccountBalances(String jsonBalances, boolean isSuccess) {
            if (InternetConnection.isNetworkAvailable(mActivity)) {
                if (!isSuccess) {
                    BinanceWidgetManager.getInstance().setUserAuthenticationForBinance(isSuccess);
                    if (cryptoWidgetBottomSheetDialogFragment != null) {
                        cryptoWidgetBottomSheetDialogFragment.dismiss();
                    }
                } else {
                    if (jsonBalances != null && !TextUtils.isEmpty(jsonBalances)) {
                        BinanceWidgetManager.getInstance().setBinanceAccountBalance(jsonBalances);
                    }
                    try {
                        BinanceWidgetManager.binanceAccountBalance = new BinanceAccountBalance(
                                BinanceWidgetManager.getInstance().getBinanceAccountBalance());
                    } catch (JSONException e) {
                        Log.e("NTP", e.getMessage());
                    }
                }
            }
            // Reset binance widget to connect page
            showWidgets();
        };
    };

    // start timer function
    public void startTimer() {
        if (countDownTimer == null) {
            countDownTimer = new Timer();
            final Handler handler = new Handler();
            countDownTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (BinanceWidgetManager.getInstance()
                                            .isUserAuthenticatedForBinance()) {
                                mBinanceNativeWorker.getAccountBalances();
                            }
                        }
                    });
                }
            }, 0, 30000);
        }
    }

    // cancel timer
    public void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer.purge();
            countDownTimer = null;
        }
    }

    public void openWidgetStack() {
        final FragmentManager fm = ((BraveActivity) mActivity).getSupportFragmentManager();
        Fragment auxiliary = new Fragment() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                fm.beginTransaction().remove(this).commit();
                if (requestCode == NTP_WIDGET_STACK_CODE) {
                    showWidgets();
                }
            }
        };
        fm.beginTransaction().add(auxiliary, "FRAGMENT_TAG").commit();
        fm.executePendingTransactions();

        Intent ntpWidgetStackActivityIntent = new Intent(mActivity, NTPWidgetStackActivity.class);
        ntpWidgetStackActivityIntent.putExtra(NTPWidgetStackActivity.FROM_SETTINGS, false);
        auxiliary.startActivityForResult(ntpWidgetStackActivityIntent, NTP_WIDGET_STACK_CODE);
    }

    @Override
    public void onTileCountChanged() {
        if (mTopsiteErrorMessage == null) {
            return;
        }

        boolean showPlaceholder =
                mTileGroup != null && mTileGroup.hasReceivedData() && mTileGroup.isEmpty();
        if (!showPlaceholder) {
            mTopsiteErrorMessage.setVisibility(View.GONE);
        } else {
            mTopsiteErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCryptoWidgetBottomSheetDialogDismiss() {
        startTimer();
    }

    @Override
    public void onCloseClick(View view) {
        Log.d("bravenews", "close click");
    }

    @Override
    public void onOptInClick(View view) {
        Log.d("bravenews", "optin click");
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d("bravenews",
                "You clicked " + adapter.getItem(position) + " on row number " + position);
    }
}

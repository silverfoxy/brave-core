package org.chromium.chrome.browser.brave_news;

import static org.chromium.ui.base.ViewUtils.dpToPx;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.app.BraveActivity;
import org.chromium.chrome.browser.brave_news.models.NewsItem;
import org.chromium.chrome.browser.util.ConfigurationUtils;
import org.chromium.ui.base.DeviceFormFactor;

import java.util.ArrayList;

public class CardBuilder { // implements ItemClickListener {

    private final int HEADLINE = 0;
    private final int HEADLINEPAIR = 1;
    private final int THREE_ROWS_PHOTO = 2;
    private final int TOP_NEWS = 3;
    private final int THREE_ROWS_HEADLINES = 4;
    private final int DISPLAY_AD = 5;
    private final int DEALS = 6;
    private final int WELCOME = 7;

    private final int CARD_LAYOUT = 0;
    private final int BUTTON_LAYOUT = 1;

    LinearLayout linearLayout;
    Activity mActivity;
    View view;
    NewsItem mNewsItem;
    int mPosition;
    int mType;
    int horizontalMargin;
    boolean isTablet;
    int mDeviceWidth;
    boolean isLandscape;

    private ItemClickListener mClickListener;
    private String TAG = "BN";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public CardBuilder(
            LinearLayout layout, Activity activity, int position, NewsItem newsItem, int type) {
        linearLayout = layout;
        mActivity = activity;
        mPosition = position;
        mType = type;
        mNewsItem = newsItem;

        // DisplayMetrics displayMetrics = new DisplayMetrics();
        // activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // int mDeviceHeight = displayMetrics.heightPixels;
        // mDeviceWidth = displayMetrics.widthPixels;
        mDeviceWidth = ConfigurationUtils.getDisplayMetrics(activity).get("width");

        isTablet = ConfigurationUtils.isTablet(
                activity); // DeviceFormFactor.isNonMultiDisplayContextOnTablet(activity);
        isLandscape = ConfigurationUtils.isLandscape(activity);

        horizontalMargin = isTablet
                ? isLandscape ? (int) (0.20 * mDeviceWidth) : (int) (0.10 * mDeviceWidth)
                : 40;

        createCard(mType, mPosition);
    }

    public void initItems() {}

    public void removeCard(LinearLayout layout) {
        layout.removeAllViews();
        layout.setVisibility(View.GONE);
        layout.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public LinearLayout createCard(int type, int position) {
        TableLayout tableLayoutTopNews = new TableLayout(mActivity);

        TableLayout.LayoutParams tableParamsTopNews = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TableLayout.LayoutParams tableParamsRow1 = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //        RecyclerView.LayoutParams linearLayoutParams = new
        //        RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
        //        RecyclerView.LayoutParams.MATCH_PARENT);
        int displayHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        TableRow rowTop = new TableRow(mActivity);
        TextView topText = new TextView(mActivity);

        Log.d("BN", "display height:" + displayHeight);
        TableRow row1 = new TableRow(mActivity);
        TableRow row2 = new TableRow(mActivity);
        TableRow row3 = new TableRow(mActivity);

        LinearLayout layout1 = new LinearLayout(mActivity);
        LinearLayout layout2 = new LinearLayout(mActivity);
        LinearLayout layout3 = new LinearLayout(mActivity);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //        linearLayoutParams.setMargins(0, 40, 0, 40);
        linearLayoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 40);

        switch (type) {
            case WELCOME:
                NestedScrollView.LayoutParams welcomeParams =
                        new NestedScrollView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                welcomeParams.setMargins(
                        horizontalMargin, displayHeight - 570, horizontalMargin, 500);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setLayoutParams(welcomeParams);
                linearLayout.setBackgroundColor(
                        mActivity.getResources().getColor(R.color.card_background));

                ImageView closeImage = new ImageView(mActivity);
                LinearLayout.LayoutParams closeImageParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                //                titleImageParams.height = 80;
                closeImageParams.gravity = Gravity.RIGHT;
                closeImageParams.setMargins(0, 10, 30, 0);
                closeImage.setLayoutParams(closeImageParams);
                closeImage.setImageResource(R.drawable.ic_close);
                closeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                linearLayout.addView(closeImage);

                ImageView titleImage = new ImageView(mActivity);
                LinearLayout.LayoutParams titleImageParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                //                titleImageParams.height = 80;
                titleImageParams.gravity = Gravity.CENTER;
                titleImageParams.setMargins(30, 2, 30, 50);
                titleImage.setLayoutParams(titleImageParams);
                titleImage.setImageResource(R.drawable.sparkling_newsreader);
                titleImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                linearLayout.addView(titleImage);

                TextView titleWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams titleWelcomeParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                titleWelcome.setTextSize(18);
                titleWelcome.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                titleWelcome.setTextColor(
                        mActivity.getResources().getColor(R.color.news_text_color));
                titleWelcome.setText(
                        "Today’s top stories in a completely private feed, just for you.");
                titleWelcome.setTypeface(null, Typeface.BOLD);
                titleWelcomeParams.gravity = Gravity.CENTER;
                titleWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                titleWelcomeParams.setMargins(40, 30, 40, 30);
                titleWelcome.setLayoutParams(titleWelcomeParams);
                linearLayout.addView(titleWelcome);

                TextView subtitleWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams subtitleWelcomeParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                subtitleWelcome.setTextSize(14);
                subtitleWelcome.setTextColor(
                        mActivity.getResources().getColor(R.color.news_text_color));
                subtitleWelcome.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                subtitleWelcome.setText(
                        "Brave Today matches your interests on your device so your personal information never leaves your browser. New content updated throughout the day.");
                subtitleWelcomeParams.gravity = Gravity.CENTER;
                subtitleWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                subtitleWelcomeParams.setMargins(40, 30, 40, 30);
                subtitleWelcome.setLayoutParams(subtitleWelcomeParams);
                linearLayout.addView(subtitleWelcome);

                Button settingsButWelcome = new Button(mActivity);
                LinearLayout.LayoutParams settingsButWelcomeParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                settingsButWelcome.setTextSize(16);
                settingsButWelcome.setAllCaps(false);
                settingsButWelcome.setText("Turn on Brave News");
                settingsButWelcomeParams.gravity = Gravity.CENTER;
                settingsButWelcome.setTextColor(
                        mActivity.getResources().getColor(R.color.news_text_color));
                settingsButWelcome.setGravity(Gravity.CENTER);
                settingsButWelcome.setPadding(40, 20, 40, 20);
                settingsButWelcomeParams.setMargins(30, 30, 30, 30);
                settingsButWelcome.setBackground(makeRound(BUTTON_LAYOUT));
                settingsButWelcome.setLayoutParams(settingsButWelcomeParams);
                settingsButWelcome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Test", "settingsButWelcome");
                        if (mClickListener != null) mClickListener.onOptInClick(view);
                    }
                });
                linearLayout.addView(settingsButWelcome);

                TextView learnMoreWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams learnMoreWelcomeParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                learnMoreWelcome.setTextSize(16);
                learnMoreWelcome.setTextColor(
                        mActivity.getResources().getColor(R.color.news_text_color));
                learnMoreWelcome.setText("Learn more about your data");
                learnMoreWelcomeParams.gravity = Gravity.CENTER;
                learnMoreWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                learnMoreWelcome.setTypeface(null, Typeface.BOLD);
                learnMoreWelcomeParams.setMargins(0, 20, 0, 80);
                learnMoreWelcome.setLayoutParams(learnMoreWelcomeParams);
                learnMoreWelcome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mClickListener != null) mClickListener.onOptInClick(view);
                    }
                });
                linearLayout.addView(learnMoreWelcome);

                break;
            case HEADLINE:
                addElementsToSingleLayout(linearLayout, position, HEADLINE);
                linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case DISPLAY_AD:
                linearLayout.setBackgroundColor(
                        mActivity.getResources().getColor(R.color.card_background));
                addElementsToSingleLayout(linearLayout, position, HEADLINE);
                linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case DEALS:
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setBackgroundColor(
                        mActivity.getResources().getColor(R.color.card_background));
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                linearLayoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 20);
                linearLayout.setLayoutParams(linearLayoutParams);

                tableLayoutTopNews.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));

                tableParamsTopNews.setMargins(20, 20, 20, 20);
                tableParamsTopNews.weight = 1;
                tableParamsTopNews.height = TableLayout.LayoutParams.WRAP_CONTENT;

                linearLayout.addView(tableLayoutTopNews);
                tableLayoutTopNews.addView(rowTop);
                rowTop.addView(topText);

                tableLayoutTopNews.addView(row1);

                row1.addView(layout1);
                row1.addView(layout2);
                row1.addView(layout3);

                rowTop.setLayoutParams(tableParamsTopNews);
                topText.setText("DEALS OF THE DAY");
                topText.setTextSize(21);
                topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                topText.setTypeface(null, Typeface.BOLD);

                layout1.setOrientation(LinearLayout.VERTICAL);
                layout2.setOrientation(LinearLayout.VERTICAL);
                layout3.setOrientation(LinearLayout.VERTICAL);

                addElementsToSingleLayout(layout1, position, DEALS);
                addElementsToSingleLayout(layout2, position + 1, DEALS);
                addElementsToSingleLayout(layout3, position + 2, DEALS);

                linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case THREE_ROWS_HEADLINES:
                Log.d(TAG, "creating HREE_ROWS_HEADLINES");

                linearLayout.setOrientation(LinearLayout.VERTICAL);
                tableLayoutTopNews.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                tableParamsTopNews.setMargins(10, 30, 5, 30);
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                linearLayoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 20);
                linearLayout.setLayoutParams(linearLayoutParams);

                linearLayout.addView(tableLayoutTopNews);
                tableLayoutTopNews.addView(rowTop);
                rowTop.addView(topText);

                tableLayoutTopNews.addView(row1);
                tableLayoutTopNews.addView(row2);
                tableLayoutTopNews.addView(row3);

                rowTop.setLayoutParams(tableParamsTopNews);
                topText.setText(mNewsItem.getPublisher_name());
                topText.setTextSize(20);
                topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                topText.setTypeface(null, Typeface.BOLD);
                //
                addElementsToSingleLayout(row1, position, type);

                row2.setPadding(5, 5, 5, 5);
                addElementsToSingleLayout(row2, position + 1, type);

                row3.setPadding(5, 5, 5, 5);
                addElementsToSingleLayout(row3, position + 2, type);

                linearLayout.setPadding(30, 10, 40, 10);
                linearLayout.setBackground(makeRound(CARD_LAYOUT));
                break;
            case TOP_NEWS:
                /*3 rows

                               TOP NEWS

                               Title           ---------
                               Description     |       |
                                               |       |
                                               ---------

                               Title           ---------
                               Description     |       |
                                               |       |
                                               --------


                               Title           ---------
                               Description     |       |
                                               |       |
                                               --------

                                               tablet landscape credit position good, first card too
                   low with int topCardMargin = ConfigurationUtils.isLandscape(mActivity) ? (int)
                   (dpHeight - 50) : (int) (dpHeight + 200); tablet portrait credit position too
                   high, first card too low

                                */

                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                linearLayout.setBackgroundColor(
                        mActivity.getResources().getColor(R.color.card_background));

                // DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float dpHeight = ConfigurationUtils.getDpDisplayMetrics(mActivity).get("height");

                LinearLayout.LayoutParams params =
                        new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT);
                Log.d("bn", "firstcard dpheight:" + dpHeight);
                Log.d("bn", "firstcard dpheight px:" + dpToPx(mActivity, dpHeight));
                Log.d("bn", "firstcard dpheight+200:" + (int) (dpHeight + 200));
                Log.d("bn", "firstcard dptopx 35:" + dpToPx(mActivity, 35));

                // int imageCreditCorrection = isLandscape ? (int) (dpHeight - 50) : (int) (dpHeight
                // + 450);
                int topCardMargin =
                        (int) (BraveActivity.getBraveActivity().getImageCreditLayoutBottom() + 20);
                Log.d("bn", "firstcard topCardMargin:" + topCardMargin);
                // if (ConfigurationUtils.isTablet(mActivity)){
                //     topCardMargin = ConfigurationUtils.isLandscape(mActivity) ? (int)
                //     (BraveActivity.getBraveActivity().getImageCreditLayoutBottom() + 50) : (int)
                //     (BraveActivity.getBraveActivity().getImageCreditLayoutBottom() + 50);
                // } else {
                //     topCardMargin = ConfigurationUtils.isLandscape(mActivity) ? (int) (dpHeight -
                //     200) : (int) (dpHeight - 20);
                // }

                // int topCardMargin = ConfigurationUtils.isLandscape(mActivity) ? (int) (dpHeight -
                // 650) : (int) (dpHeight - 200);

                params.setMargins(horizontalMargin, 0, horizontalMargin, 40);
                int height = mActivity.getResources().getDisplayMetrics().heightPixels;
                linearLayout.setLayoutParams(params);
                Log.d("bn",
                        "newlayout display height:"
                                + ConfigurationUtils.getDisplayMetrics(mActivity).get("height"));
                Log.d("bn", "newlayout linearLayout.getBottom:" + linearLayout.getBottom());
                linearLayout.addView(tableLayoutTopNews);
                tableLayoutTopNews.addView(rowTop);
                rowTop.addView(topText);

                tableLayoutTopNews.addView(row1);
                tableLayoutTopNews.addView(row2);
                tableLayoutTopNews.addView(row3);

                linearLayout.setPadding(30, 10, 40, 10);
                tableParamsTopNews.setMargins(0, 0, 0, 40);
                rowTop.setLayoutParams(tableParamsTopNews);
                topText.setText("TOP NEWS");
                topText.setTextSize(28);
                topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                topText.setTypeface(null, Typeface.BOLD);

                tableParamsTopNews.weight = 3;

                addElementsToSingleLayout(row1, position, THREE_ROWS_PHOTO);
                addElementsToSingleLayout(row2, position + 1, THREE_ROWS_PHOTO);
                addElementsToSingleLayout(row3, position + 2, THREE_ROWS_PHOTO);
                linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case HEADLINEPAIR:
                /*headlinepair

                             Image      Image
                           ---------    ---------
                           |       |    |       |
                           |       |    |       |
                           ---------    ---------
                             Title        Title
                          Description    Description

                        */
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setBackgroundColor(Color.TRANSPARENT);
                linearLayoutParams.height = 650;
                linearLayoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 20);
                linearLayout.setLayoutParams(linearLayoutParams);

                LinearLayout.LayoutParams cellParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cellParams.setMargins(0, 0, 20, 0);
                LinearLayout layoutLeft = new LinearLayout(mActivity);
                LinearLayout layoutRight = new LinearLayout(mActivity);
                linearLayout.addView(layoutLeft);
                layoutLeft.setLayoutParams(cellParams);
                layoutLeft.setOrientation(LinearLayout.VERTICAL);
                //                layoutLeft.setPadding(10, 10, 10, 10);
                //                layoutLeft.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));
                addElementsToSingleLayout(layoutLeft, position, type);

                //                LinearLayout layoutRight = new LinearLayout(mActivity);
                linearLayout.addView(layoutRight);
                cellParams.setMargins(20, 0, 0, 0);
                layoutRight.setLayoutParams(cellParams);
                layoutRight.setOrientation(LinearLayout.VERTICAL);
                //                layoutLeft.setPadding(10, 10, 10, 10);
                //                layoutRight.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));

                addElementsToSingleLayout(layoutRight, position + 1, type);
                break;
        }

        linearLayout.setBackground(makeRound(CARD_LAYOUT));

        return linearLayout;
    }

    private LayerDrawable makeRound(int type) {
        float[] outerRadii = new float[] {15, 15, 15, 15, 15, 15, 15, 15};
        float[] innerRadii = new float[] {15, 15, 15, 15, 15, 15, 15, 15};

        // Set the shape background
        ShapeDrawable backgroundShape = null;
        new ShapeDrawable(new RoundRectShape(outerRadii, null, innerRadii));

        if (type == BUTTON_LAYOUT) {
            outerRadii = new float[] {75, 75, 75, 75, 75, 75, 75, 75};
            innerRadii = new float[] {75, 75, 75, 75, 75, 75, 75, 75};
            backgroundShape = new ShapeDrawable(new RoundRectShape(outerRadii, null, innerRadii));
            backgroundShape.getPaint().setColor(
                    mActivity.getResources().getColor(R.color.turn_on_button)); // background color
            backgroundShape.getPaint().setStyle(Paint.Style.FILL); // Define background
            backgroundShape.getPaint().setAntiAlias(true);
            backgroundShape.setPadding(50, 20, 50, 20);

        } else if (type == CARD_LAYOUT) {
            backgroundShape = new ShapeDrawable(new RoundRectShape(outerRadii, null, innerRadii));
        }

        // Initialize an array of drawables
        Drawable[] drawables = new Drawable[] {backgroundShape};
        // Initialize a layer drawable object
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        return layerDrawable;
    }

    private void addElementsToSingleLayout(ViewGroup view, int position, int itemType) {
        ImageView image = new ImageView(mActivity);
        ImageView logo = new ImageView(mActivity);
        TextView title = new TextView(mActivity);
        TextView source = new TextView(mActivity);
        TextView desc = new TextView(mActivity);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams headLineParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        TableLayout.LayoutParams tableParamsTopNews = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TableRow.LayoutParams linearLayoutRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams linearLayoutRowParams1 = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams sourceRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams titleRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams descRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams imageRowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        LinearLayout layou1 = new LinearLayout(mActivity);

        //        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        //                LinearLayout.LayoutParams.MATCH_PARENT,
        //                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout layout = (LinearLayout) view;
        switch (itemType) {
            case WELCOME:

                break;
            case DEALS:
                LinearLayout layoutDeals = (LinearLayout) view;
                layoutDeals.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams1.width = 0;
                linearLayoutRowParams1.weight = 1;
                linearLayoutRowParams1.height = 600;
                linearLayoutRowParams1.setMargins(20, 0, 20, 0);
                layoutDeals.setLayoutParams(linearLayoutRowParams1);

                image.setImageResource(R.drawable.img1);
                imageParams.height = 0;
                imageParams.weight = 4;
                imageParams.bottomMargin = 20;
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setLayoutParams(imageParams);
                layoutDeals.addView(image);

                titleParams.height = 0;
                titleParams.weight = 3;
                title.setTextSize(14);
                title.setTypeface(null, Typeface.BOLD);
                title.setPadding(10, 10, 10, 0);
                title.setLayoutParams(titleParams);
                title.setMaxLines(3);

                title.setEllipsize(TextUtils.TruncateAt.END);
                layoutDeals.addView(title);

                descParams.weight = 2;
                descParams.height = 0;
                desc.setTextSize(12);
                descParams.bottomMargin = 60;
                desc.setLayoutParams(descParams);

                desc.setPadding(10, 0, 30, 30);
                layoutDeals.addView(desc);

                break;
            case HEADLINE:
                layout.setOrientation(LinearLayout.VERTICAL);
                linearLayoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayoutParams.setMargins(horizontalMargin, 20, horizontalMargin, 50);
                layout.setLayoutParams(linearLayoutParams);

                layout.addView(image);
                layout.addView(title);
                layout.addView(desc);
                layout.addView(logo);
                //
                imageParams.height = 0;
                imageParams.weight = 7;
                image.setLayoutParams(imageParams);
                image.setImageResource(R.drawable.img2);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                titleParams.height = 0;
                titleParams.weight = 3;
                titleParams.rightMargin = 20;
                title.setTextSize(18);
                title.setLines(3);
                title.setEllipsize(TextUtils.TruncateAt.END);
                title.setPadding(30, 30, 30, 0);
                title.setLayoutParams(titleParams);

                descParams.weight = 1;
                descParams.height = 0;
                desc.setTextSize(11);
                desc.setLayoutParams(descParams);
                desc.setPadding(30, 0, 30, 30);

                logoParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                logoParams.height = 0;
                logoParams.weight = 1;
                logo.setPadding(30, 0, 30, 40);
                logoParams.gravity = Gravity.CENTER_VERTICAL;
                logo.setScaleType(ImageView.ScaleType.FIT_START);
                logo.setImageResource(R.drawable.logo);
                logo.setLayoutParams(logoParams);

                break;
            case HEADLINEPAIR:
                layout.setOrientation(LinearLayout.VERTICAL);
                imageParams.weight = 6;
                imageParams.height = 0;
                image.setLayoutParams(imageParams);
                image.setImageResource(R.drawable.img2);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layout.addView(image);

                titleParams.weight = 4;
                titleParams.height = 0;
                //                title.setGravity(Gravity.CENTER_VERTICAL);
                //                titleParams.setMargins(0, 10, 0, 0);
                title.setLayoutParams(titleParams);
                title.setTextSize(14);
                title.setLines(2);
                title.setEllipsize(TextUtils.TruncateAt.END);
                title.setPadding(10, 40, 10, 0);
                layout.addView(title);

                descParams.weight = 1;
                descParams.height = 0;
                desc.setTextSize(11);
                //                desc.setGravity(Gravity.TOP);
                //                params.setMargins(0,0,0,0);
                desc.setLayoutParams(descParams);
                desc.setPadding(10, 0, 10, 10);
                layout.addView(desc);

                logoParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                logoParams.height = 0;
                logoParams.weight = 1;
                logoParams.gravity = Gravity.CENTER_VERTICAL;
                //                params.setMargins(0,0,0,30);
                logo.setScaleType(ImageView.ScaleType.FIT_START);
                logo.setPadding(10, 00, 10, 10);
                logo.setImageResource(R.drawable.logo);
                logo.setLayoutParams(logoParams);
                layout.addView(logo);

                break;

            case THREE_ROWS_HEADLINES:

                TableRow layoutRow = (TableRow) view;

                layoutRow.removeAllViews();
                layou1.removeAllViews();

                layou1.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams.width = 0;
                linearLayoutRowParams.weight = 1;

                layou1.setLayoutParams(linearLayoutRowParams);

                titleRowParams.height = 0;
                titleRowParams.weight = 2;
                titleRowParams.rightMargin = 20;
                title.setTextSize(17);
                title.setLayoutParams(titleRowParams);
                title.setLines(2);
                title.setEllipsize(TextUtils.TruncateAt.END);
                layou1.addView(title);

                descRowParams.height = 0;
                descRowParams.weight = 1;
                descRowParams.bottomMargin = 10;
                desc.setLayoutParams(descRowParams);
                layou1.addView(desc);

                layoutRow.addView(layou1);
                break;
            case THREE_ROWS_PHOTO:
                TableRow layoutRowPhotos = (TableRow) view;

                tableParamsTopNews.setMargins(0, 20, 0, 20);
                tableParamsTopNews.weight = 2;
                tableParamsTopNews.height = 0;
                layoutRowPhotos.setLayoutParams(tableParamsTopNews);

                layou1.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams.width = 0;
                linearLayoutRowParams.weight = 2;
                layou1.setLayoutParams(linearLayoutRowParams);

                sourceRowParams.height = 0;
                sourceRowParams.weight = 1;
                source.setLayoutParams(sourceRowParams);
                source.setText(mNewsItem.getPublisher_name());
                source.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                layou1.addView(source);

                titleRowParams.height = 0;
                titleRowParams.weight = 3;
                titleRowParams.rightMargin = 20;
                title.setTextSize(17);
                title.setLayoutParams(titleRowParams);
                title.setLines(3);
                title.setEllipsize(TextUtils.TruncateAt.END);
                layou1.addView(title);

                descRowParams.height = 0;
                descRowParams.weight = 1;
                descRowParams.bottomMargin = 10;
                desc.setLayoutParams(descRowParams);
                layou1.addView(desc);

                layoutRowPhotos.addView(layou1);

                imageRowParams.width = 0;
                imageRowParams.height = 300;

                image.setMinimumHeight(200);
                imageRowParams.weight = 1;
                imageRowParams.setMargins(0, 0, 0, 30);
                image.setScaleType(ImageView.ScaleType.CENTER);
                image.setImageResource(R.drawable.img3);

                image.setLayoutParams(imageRowParams);
                layoutRowPhotos.addView(image);

                break;
            default:
                image.setImageResource(R.drawable.img4);
                title.setText(mNewsItem.getTitle());
                desc.setText(mNewsItem.getDescription());

                layout.addView(image);
                layout.addView(title);
                layout.addView(desc);
                break;
        }

        title.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
        desc.setTextColor(mActivity.getResources().getColor(R.color.news_time_color));

        // this section is common to all cases
        title.setText(mNewsItem.getTitle());
        desc.setText(mNewsItem.getPublish_time());

        //        PrivateCDNHelper privateHelper = new PrivateCDNHelper(image);
        //        privateHelper.setPaddedImage(image, mNewsItems.get(position).getPadded_img());
        //  to add when there is an image      image.setImageResource(R.drawable.img4);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Test", "click itemid:" + (mNewsItem.getUrl()));
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(mNewsItem.getUrl()));
                mActivity.startActivity(browserIntent);
            }
        });
    }

    // allows clicks events to be caught
    void setCardClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //
    //    @Override
    //    public void onClick(View v) {
    //        if (mClickListener != null) mClickListener.onItemClick(view, 1);
    //    }
    //
    //    @Override
    //    public void onItemClick(View view, int position) {
    //
    //    }
    //
    //    @Override
    //    public void onOptInClick(View view) {
    //
    //    }
    //
    //    @Override
    //    public void onCloseClick(View view) {
    //
    //    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);

        void onOptInClick(View view);

        void onCloseClick(View view);
    }
}

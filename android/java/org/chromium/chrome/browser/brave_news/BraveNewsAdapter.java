package org.chromium.chrome.browser.brave_news;

import android.app.Activity;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.chromium.chrome.R;
import org.chromium.chrome.browser.brave_news.models.NewsItem;
import org.chromium.chrome.browser.ntp.BraveNewTabPageLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BraveNewsAdapter extends RecyclerView.Adapter<BraveNewsAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    public RecycleItemClickListener mClickListener;
    private int mType;
    Context mContext;
    Activity mActivity;
    View view;
    LinearLayout linearLayout;
    LinearLayout.LayoutParams linearLayoutParams;
    CopyOnWriteArrayList<NewsItem> mNewsItems;

    int cardPosition = 0;
    private boolean isOptIn = false;

    private final int CARD_LAYOUT = 0;
    private final int BUTTON_LAYOUT = 1;

    private final int HEADLINE = 0;
    private final int HEADLINEPAIR = 1;
    private final int THREE_ROWS_PHOTO = 2;
    private final int TOP_NEWS = 3;
    private final int THREE_ROWS_HEADLINES = 4;
    private final int DISPLAY_AD = 5;
    private final int DEALS = 6;
    private final int WELCOME = 7;

    ViewHolder mHolder;
    private final String TAG = "BNA";


    public BraveNewsAdapter(Activity activity, CopyOnWriteArrayList<NewsItem> newsItems) {
        this.mInflater = LayoutInflater.from(activity);
        this.mActivity = activity;
        this.mNewsItems = newsItems;
    }
    // data is passed into the constructor

    private int getHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        return height;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = mInflater.inflate(R.layout.brave_news_row, parent, false);
        mHolder = new ViewHolder(view);
        return mHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull BraveNewsAdapter.ViewHolder holder, int position) {

        if (position >= getItemCount() - 10) {
            Log.d(TAG, "end");
            return;
        }
        LinearLayout.LayoutParams params1;

//        Log.d(TAG, "optedin:" + isOptIn);

//        if (!isOptIn) {
//            createCard(WELCOME, position);
//        } else
//            {
//            linearLayoutParams.setMargins(40, 0, 40, 40);
        switch (cardPosition) {
            case 1:
                //TOP news
//                    createCard(WELCOME, position);
                Log.d(TAG, "creating TOP_NEWS");
                createCard(TOP_NEWS, position);
                break;
            case 2:
                Log.d(TAG, "creating HEADLINE");
                createCard(HEADLINE, position);
                // Display Ad
                break;
            case 3:
            case 4:
                /* headline

                      Image
                    -------------
                    |           |
                    |           |
                    -------------
                      Title
                      Description

                */
                createCard(HEADLINE, position);
                break;
            case 5:
                /*headlinepair

                      Image      Image
                    ---------    ---------
                    |       |    |       |
                    |       |    |       |
                    ---------    ---------
                      Title        Title
                   Description    Description

                 */

                createCard(HEADLINEPAIR, position);

                break;
            case 6:
                // promoted content
                createCard(HEADLINE, position);
                break;
            case 7:
                // 3x Headlines
                createCard(THREE_ROWS_HEADLINES, position);
                break;
            case 8:
            case 9:
                createCard(DEALS, position);
                // headline
                break;
            case 10:
            case 11:
                // 2 col headline
                break;
            case 12:
                // Display Ad
                break;
            case 13:
            case 14:
                // Headline
                break;
            case 15:
                // Publisher card (3x cards)
                break;
            case 16:
                // List card (3x cards)
                break;
            case 17:
                // Headline
                break;
            case 18:
                // Offers card
                break;
            case 19:
                //Headline
                break;
            case 20:
                // 2 col Headline
                break;
        }
        if (position % 30 == 0) {
            cardPosition = 0;
        }
        cardPosition++;
    }


    @Override
    public int getItemCount() {
        return mNewsItems.size();
    }

    private void initHolderViews() {
        mHolder.layou1 = new LinearLayout(mActivity);
        mHolder.layoutLeft = new LinearLayout(mActivity);
        mHolder.layoutRight = new LinearLayout(mActivity);
        mHolder.title = new TextView(mActivity);
        mHolder.desc = new TextView(mActivity);

        mHolder.image = new ImageView(mActivity);
        mHolder.logo = new ImageView(mActivity);
        mHolder.source = new TextView(mActivity);
        mHolder.layoutRowPhotos = new TableRow(mActivity);
        mHolder.layoutRow = new TableRow(mActivity);
        mHolder.row1 = new TableRow(mActivity);
        mHolder.row2 = new TableRow(mActivity);
        mHolder.row3 = new TableRow(mActivity);
        mHolder.layout1 = new LinearLayout(mActivity);
        mHolder.layout2 = new LinearLayout(mActivity);
        mHolder.layout3 = new LinearLayout(mActivity);
        mHolder.layout = new LinearLayout(mActivity);

        mHolder.tableLayoutTopNews = new TableLayout(mActivity);

        mHolder.rowTop = new TableRow(mActivity);
        mHolder.topText = new TextView(mActivity);

    }

    public void setClickListener(BraveNewTabPageLayout recyclerMain) {
        this.mClickListener = recyclerMain;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layoutRight;
        LinearLayout layoutLeft;
        TextView myTextView;
        TextView testTextView;
        LinearLayout testLinear;

        TableRow rowTop;
        TextView topText;

        ImageView image;
        ImageView logo;
        TextView title;
        TextView source;
        TextView desc;
        LinearLayout layou1;

        TableRow layoutRowPhotos;
        TableRow layoutRow;

        TableRow row1;
        TableRow row2;
        TableRow row3;

        LinearLayout layout1;
        LinearLayout layout2;
        LinearLayout layout3;

        LinearLayout layout;

        TableLayout tableLayoutTopNews;

        LinearLayout linearLayout;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.item);
            testTextView = new TextView(mActivity);
            testLinear = new LinearLayout(mActivity);

            layou1 = new LinearLayout(mActivity);
            title = new TextView(mActivity);
            desc = new TextView(mActivity);

            image = new ImageView(mActivity);
            logo = new ImageView(mActivity);
            source = new TextView(mActivity);
            layoutRowPhotos = new TableRow(mActivity);
            layoutRow = new TableRow(mActivity);
            row1 = new TableRow(mActivity);
            row2 = new TableRow(mActivity);
            row3 = new TableRow(mActivity);
            layout1 = new LinearLayout(mActivity);
            layout2 = new LinearLayout(mActivity);
            layout3 = new LinearLayout(mActivity);
            layout = new LinearLayout(mActivity);

            tableLayoutTopNews = new TableLayout(mActivity);

            rowTop = new TableRow(mActivity);
            topText = new TextView(mActivity);


            itemView.setOnClickListener(this);
            linearLayout = itemView.findViewById(R.id.card_layout);

            linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutParams.setMargins(0, 40, 0, 40);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createCard(int type, int position) {
//        TableLayout tableLayoutTopNews = new TableLayout(mActivity);
        TableLayout.LayoutParams tableParamsTopNews = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TableLayout.LayoutParams tableParamsRow1 = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        RecyclerView.LayoutParams linearLayoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);

//        initHolderViews();

        switch (type) {
            case WELCOME:
                mHolder.linearLayout.setOrientation(LinearLayout.VERTICAL);
                mHolder.linearLayout.setLayoutParams(linearLayoutParams);
                mHolder.linearLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));

                ImageView closeImage = new ImageView(mActivity);
                LinearLayout.LayoutParams closeImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
//                titleImageParams.height = 80;
                closeImageParams.gravity = Gravity.RIGHT;
                closeImageParams.setMargins(0, 10, 30, 0);
                closeImage.setLayoutParams(closeImageParams);
                closeImage.setImageResource(R.drawable.ic_close);
                closeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mHolder.linearLayout.addView(closeImage);

                ImageView titleImage = new ImageView(mActivity);
                LinearLayout.LayoutParams titleImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
//                titleImageParams.height = 80;
                titleImageParams.gravity = Gravity.CENTER;
                titleImageParams.setMargins(30, 2, 30, 50);
                titleImage.setLayoutParams(titleImageParams);
                titleImage.setImageResource(R.drawable.sparkling_newsreader);
                titleImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mHolder.linearLayout.addView(titleImage);

                TextView titleWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams titleWelcomeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                titleWelcome.setTextSize(18);
                titleWelcome.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                titleWelcome.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                titleWelcome.setText("Today’s top stories in a completely private feed, just for you.");
                titleWelcome.setTypeface(null, Typeface.BOLD);
                titleWelcomeParams.gravity = Gravity.CENTER;
                titleWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                titleWelcomeParams.setMargins(40, 30, 40, 30);
                titleWelcome.setLayoutParams(titleWelcomeParams);
                mHolder.linearLayout.addView(titleWelcome);

                TextView subtitleWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams subtitleWelcomeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                subtitleWelcome.setTextSize(14);
                subtitleWelcome.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                subtitleWelcome.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                subtitleWelcome.setText("Brave Today matches your interests on your device so your personal information never leaves your browser. New content updated throughout the day.");
                subtitleWelcomeParams.gravity = Gravity.CENTER;
                subtitleWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                subtitleWelcomeParams.setMargins(40, 30, 40, 30);
                subtitleWelcome.setLayoutParams(subtitleWelcomeParams);
                mHolder.linearLayout.addView(subtitleWelcome);

                Button settingsButWelcome = new Button(mActivity);
                LinearLayout.LayoutParams settingsButWelcomeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                settingsButWelcome.setTextSize(16);
                settingsButWelcome.setAllCaps(false);
                settingsButWelcome.setText("Turn on Brave News");
                settingsButWelcomeParams.gravity = Gravity.CENTER;
                settingsButWelcome.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                settingsButWelcome.setGravity(Gravity.CENTER);
                settingsButWelcome.setPadding(40, 20, 40, 20);
                settingsButWelcomeParams.setMargins(30, 30, 30, 30);
                settingsButWelcome.setBackground(makeRound(BUTTON_LAYOUT));
                settingsButWelcome.setLayoutParams(settingsButWelcomeParams);
                settingsButWelcome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mClickListener != null) mClickListener.onOptInClick(view);
                    }
                });
                mHolder.linearLayout.addView(settingsButWelcome);

                TextView learnMoreWelcome = new TextView(mActivity);
                LinearLayout.LayoutParams learnMoreWelcomeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                learnMoreWelcome.setTextSize(16);
                learnMoreWelcome.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                learnMoreWelcome.setText("Learn more about your data");
                learnMoreWelcomeParams.gravity = Gravity.CENTER;
                learnMoreWelcome.setGravity(Gravity.CENTER_HORIZONTAL);
                learnMoreWelcome.setTypeface(null, Typeface.BOLD);
                learnMoreWelcomeParams.setMargins(0, 20, 0, 80);
                learnMoreWelcome.setLayoutParams(learnMoreWelcomeParams);
                learnMoreWelcome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isOptIn = true;
                        if (mClickListener != null) mClickListener.onOptInClick(view);
                    }
                });

                mHolder.linearLayout.addView(learnMoreWelcome);

                break;
            case HEADLINE:

                addElementsToSingleLayout(mHolder.linearLayout, position, HEADLINE);
                mHolder.linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case DISPLAY_AD:
                mHolder.linearLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));
                addElementsToSingleLayout(linearLayout, position, HEADLINE);
                mHolder.linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case DEALS:
                mHolder.linearLayout.setOrientation(LinearLayout.VERTICAL);
                mHolder.linearLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                linearLayoutParams.setMargins(40, 0,40,20);
                mHolder.linearLayout.setLayoutParams(linearLayoutParams);

                mHolder.tableLayoutTopNews.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                tableParamsTopNews.setMargins(20, 20, 20, 20);
                tableParamsTopNews.weight = 1;
                tableParamsTopNews.height = TableLayout.LayoutParams.WRAP_CONTENT;


                mHolder.linearLayout.addView(mHolder.tableLayoutTopNews);
                mHolder.tableLayoutTopNews.addView(mHolder.rowTop);
                mHolder.rowTop.addView(mHolder.topText);

                mHolder.tableLayoutTopNews.addView(mHolder.row1);

                mHolder.row1.addView(mHolder.layout1);
                mHolder.row1.addView(mHolder.layout2);
                mHolder.row1.addView(mHolder.layout3);

                mHolder.rowTop.setLayoutParams(tableParamsTopNews);
                mHolder.topText.setText("DEALS OF THE DAY");
                mHolder.topText.setTextSize(21);
                mHolder.topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                mHolder.topText.setTypeface(null, Typeface.BOLD);

                mHolder.layout1.setOrientation(LinearLayout.VERTICAL);
                mHolder.layout2.setOrientation(LinearLayout.VERTICAL);
                mHolder.layout3.setOrientation(LinearLayout.VERTICAL);

                addElementsToSingleLayout(mHolder.layout1, position, DEALS);
                addElementsToSingleLayout(mHolder.layout2, position + 1, DEALS);
                addElementsToSingleLayout(mHolder.layout3, position + 2, DEALS);

                mHolder.linearLayout.setBackground(makeRound(CARD_LAYOUT));

                break;
            case THREE_ROWS_HEADLINES:
                Log.d(TAG, "creating HREE_ROWS_HEADLINES");

                mHolder.linearLayout.setOrientation(LinearLayout.VERTICAL);
                mHolder.tableLayoutTopNews.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                tableParamsTopNews.setMargins(10, 30, 5, 30);
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                linearLayoutParams.setMargins(40, 0,40,20);
                mHolder.linearLayout.setLayoutParams(linearLayoutParams);

                mHolder.linearLayout.addView(mHolder.tableLayoutTopNews);
                mHolder.tableLayoutTopNews.addView(mHolder.rowTop);
                mHolder.rowTop.addView(mHolder.topText);

                mHolder.tableLayoutTopNews.addView(mHolder.row1);
                mHolder.tableLayoutTopNews.addView(mHolder.row2);
                mHolder.tableLayoutTopNews.addView(mHolder.row3);

                mHolder.rowTop.setLayoutParams(tableParamsTopNews);
                mHolder.topText.setText(mNewsItems.get(position).getPublisher_name());
                mHolder.topText.setTextSize(20);
                mHolder.topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                mHolder.topText.setTypeface(null, Typeface.BOLD);
//
                addElementsToSingleLayout(mHolder.row1, position, type);

                mHolder.row2.setPadding(5, 5, 5, 5);
                addElementsToSingleLayout(mHolder.row2, position + 1, type);

                mHolder.row3.setPadding(5, 5, 5, 5);
                addElementsToSingleLayout(mHolder.row3, position + 2, type);

                mHolder.linearLayout.setPadding(30, 10, 40, 10);
                mHolder.linearLayout.setBackground(makeRound(CARD_LAYOUT));
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

                 */

                mHolder.linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                mHolder.linearLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));

                LinearLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(40,-300,40,40);
                int height = mActivity.getResources().getDisplayMetrics().heightPixels;
                mHolder.linearLayout.setLayoutParams(params);

                mHolder.linearLayout.addView(mHolder.tableLayoutTopNews);
                mHolder.tableLayoutTopNews.addView(mHolder.rowTop);
                mHolder.rowTop.addView(mHolder.topText);

                mHolder.tableLayoutTopNews.addView(mHolder.row1);
                mHolder.tableLayoutTopNews.addView(mHolder.row2);
                mHolder.tableLayoutTopNews.addView(mHolder.row3);

                mHolder.linearLayout.setPadding(30, 10, 40, 10);
                tableParamsTopNews.setMargins(0,0,0,40);
                mHolder.rowTop.setLayoutParams(tableParamsTopNews);
                mHolder.topText.setText("TOP NEWS");
                mHolder.topText.setTextSize(28);
                mHolder.topText.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
                mHolder.topText.setTypeface(null, Typeface.BOLD);

                tableParamsTopNews.weight = 3;

                addElementsToSingleLayout(mHolder.row1, position, THREE_ROWS_PHOTO);
                addElementsToSingleLayout(mHolder.row2, position + 1, THREE_ROWS_PHOTO);
                addElementsToSingleLayout(mHolder.row3, position + 2, THREE_ROWS_PHOTO);
                mHolder.linearLayout.setBackground(makeRound(CARD_LAYOUT));


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
                mHolder.linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHolder.linearLayout.setBackgroundColor(Color.TRANSPARENT);
                linearLayoutParams.height = 650;
                linearLayoutParams.setMargins(40, 0,40, 20);
                mHolder.linearLayout.setLayoutParams(linearLayoutParams);

                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                cellParams.setMargins(0, 0, 20, 0);
                mHolder.layoutLeft = new LinearLayout(mActivity);
                mHolder.layoutRight = new LinearLayout(mActivity);
                mHolder.linearLayout.addView(mHolder.layoutLeft);
                mHolder.layoutLeft.setLayoutParams(cellParams);
                mHolder.layoutLeft.setOrientation(LinearLayout.VERTICAL);
//                layoutLeft.setPadding(10, 10, 10, 10);
//                mHolder.layoutLeft.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));
                addElementsToSingleLayout(mHolder.layoutLeft, position, type);

//                LinearLayout layoutRight = new LinearLayout(mActivity);
                mHolder.linearLayout.addView(mHolder.layoutRight);
                cellParams.setMargins(20, 0, 0, 0);
                mHolder.layoutRight.setLayoutParams(cellParams);
                mHolder.layoutRight.setOrientation(LinearLayout.VERTICAL);
//                layoutLeft.setPadding(10, 10, 10, 10);
//                mHolder.layoutRight.setBackgroundColor(mActivity.getResources().getColor(R.color.card_background));

                addElementsToSingleLayout(mHolder.layoutRight, position + 1, type);
                break;
        }


    }


    private void addElementsToSingleLayout(ViewGroup view, int position, int itemType) {

//        ImageView image = new ImageView(mActivity);
//        ImageView logo = new ImageView(mActivity);
//        TextView title = new TextView(mActivity);
//        TextView source = new TextView(mActivity);
//        TextView desc = new TextView(mActivity);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams headLineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        TableLayout.LayoutParams tableParamsTopNews = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TableRow.LayoutParams linearLayoutRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams linearLayoutRowParams1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams sourceRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams titleRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams descRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams imageRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

//        initHolderViews();

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        switch (itemType) {
            case WELCOME:

                break;
            case DEALS:

                mHolder.image = new ImageView(mActivity);
                mHolder.title = new TextView(mActivity);
                mHolder.desc = new TextView(mActivity);

//                LinearLayout layoutDeals = (LinearLayout) view;

                view.addView(mHolder.image);
                view.addView(mHolder.title);
                view.addView(mHolder.desc);

//                view.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams1.width = 0;
                linearLayoutRowParams1.weight = 1;
                linearLayoutRowParams1.height = 600;
                linearLayoutRowParams1.setMargins(20, 0, 20, 0);
                view.setLayoutParams(linearLayoutRowParams1);

                mHolder.image.setImageResource(R.drawable.img2);
                imageParams.height = 0;
                imageParams.weight = 4;
                imageParams.bottomMargin = 20;
                mHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mHolder.image.setLayoutParams(imageParams);

                titleParams.height = 0;
                titleParams.weight = 3;
                mHolder.title.setTextSize(14);
                mHolder.title.setTypeface(null, Typeface.BOLD);
                mHolder.title.setPadding(10, 10, 10, 0);
                mHolder.title.setLayoutParams(titleParams);
                mHolder.title.setMaxLines(3);

//                title.setBackgroundColor(mActivity.getResources().getColor(R.color.purple_500));
                mHolder.title.setEllipsize(TextUtils.TruncateAt.END);

                descParams.weight = 2;
                descParams.height = 0;
                mHolder.desc.setTextSize(12);
                descParams.bottomMargin = 60;
                mHolder.desc.setLayoutParams(descParams);
//                desc.setBackgroundColor(mActivity.getResources().getColor(R.color.purple_200));

                mHolder.desc.setPadding(10, 0, 30, 0);

                break;
            case HEADLINE:
                linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayoutParams.setMargins(50, 20, 40, 50);
                mHolder.linearLayout.setLayoutParams(linearLayoutParams);
                mHolder.linearLayout.removeAllViews();

                mHolder.linearLayout.addView(mHolder.image);
                mHolder.linearLayout.addView(mHolder.title);
                mHolder.linearLayout.addView(mHolder.desc);
                mHolder.linearLayout.addView(mHolder.logo);

                mHolder.linearLayout.setOrientation(LinearLayout.VERTICAL);
//
                imageParams.height = 0;
                imageParams.weight = 7;
                mHolder.image.setLayoutParams(imageParams);
                mHolder.image.setImageResource(R.drawable.img1);
                mHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                titleParams.height = 0;
                titleParams.weight = 3;
                titleParams.rightMargin = 20;
                mHolder.title.setTextSize(18);
                mHolder.title.setLines(3);
                mHolder.title.setEllipsize(TextUtils.TruncateAt.END);
                mHolder.title.setPadding(30, 30, 30, 0);
                mHolder.title.setLayoutParams(titleParams);

                descParams.weight = 1;
                descParams.height = 0;
                mHolder.desc.setTextSize(11);
                mHolder.desc.setLayoutParams(descParams);
                mHolder.desc.setPadding(30, 0, 30, 30);

                logoParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                logoParams.height = 0;
                logoParams.weight = 1;
                mHolder.logo.setPadding(30, 0, 30, 40);
                logoParams.gravity = Gravity.CENTER_VERTICAL;
                mHolder.logo.setScaleType(ImageView.ScaleType.FIT_START);
                mHolder.logo.setImageResource(R.drawable.logo);
                mHolder.logo.setLayoutParams(logoParams);

                break;
            case HEADLINEPAIR:
                view.removeAllViews();
//                mHolder.layout.setOrientation(LinearLayout.VERTICAL);
//
                mHolder.logo = new ImageView(mActivity);
                mHolder.image = new ImageView(mActivity);
                mHolder.title = new TextView(mActivity);
                mHolder.desc = new TextView(mActivity);

                view.addView(mHolder.image);
                view.addView(mHolder.title);
                view.addView(mHolder.desc);
                view.addView(mHolder.logo);

                imageParams.weight = 6;
                imageParams.height = 0;
                mHolder.image.setLayoutParams(imageParams);
                mHolder.image.setImageResource(R.drawable.img3);
                mHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                titleParams.weight = 4;
                titleParams.height = 0;
                mHolder.title.setLayoutParams(titleParams);
                mHolder.title.setTextSize(14);
                mHolder.title.setLines(2);
                mHolder.title.setEllipsize(TextUtils.TruncateAt.END);
                mHolder.title.setPadding(0, 40, 10, 0);

                descParams.weight = 1;
                descParams.height = 0;
                mHolder.desc.setTextSize(11);
                mHolder.desc.setLayoutParams(descParams);
                mHolder.desc.setPadding(0, 0, 10, 10);

                logoParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                logoParams.height = 0;
                logoParams.weight = 1;
                logoParams.gravity = Gravity.CENTER_VERTICAL;
                mHolder.logo.setScaleType(ImageView.ScaleType.FIT_START);
                mHolder.logo.setPadding(0, 0, 10, 10);
                mHolder.logo.setImageResource(R.drawable.logo);
                mHolder.logo.setLayoutParams(logoParams);
                view.setBackground(makeRound(CARD_LAYOUT));

                break;
            case THREE_ROWS_HEADLINES:

                mHolder.layou1 = new LinearLayout(mActivity);
                mHolder.title = new TextView(mActivity);
                mHolder.desc = new TextView(mActivity);
                view.removeAllViews();
                view.addView(mHolder.layou1);
                mHolder.layou1.addView(mHolder.title);
                mHolder.layou1.addView(mHolder.desc);

                mHolder.layou1.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams.width = 0;
                linearLayoutRowParams.weight = 1;

                mHolder.layou1.setLayoutParams(linearLayoutRowParams);

                titleRowParams.height = 0;
                titleRowParams.weight = 2;
                titleRowParams.rightMargin = 20;
                mHolder.title.setTextSize(17);
                mHolder.title.setLayoutParams(titleRowParams);
                mHolder.title.setLines(2);
                mHolder.title.setEllipsize(TextUtils.TruncateAt.END);

                descRowParams.height = 0;
                descRowParams.weight = 1;
                descRowParams.bottomMargin = 10;
                mHolder.desc.setLayoutParams(descRowParams);

                break;
            case THREE_ROWS_PHOTO:
//                view.removeAllViews();
//                mHolder.layoutRowPhotos = (TableRow) view;
                mHolder.layou1 = new LinearLayout(mActivity);
                mHolder.source = new TextView(mActivity);
                mHolder.title = new TextView(mActivity);
                mHolder.desc = new TextView(mActivity);
                mHolder.image = new ImageView(mActivity);
                view.removeAllViews();
                view.addView(mHolder.layou1);
                mHolder.layou1.addView(mHolder.source);
                mHolder.layou1.addView(mHolder.title);
                mHolder.layou1.addView(mHolder.desc);
                view.addView(mHolder.image);
                Log.d(TAG, "rootview:" + mHolder.layoutRowPhotos.getChildCount());

                tableParamsTopNews.setMargins(0, 20, 0, 20);
                tableParamsTopNews.weight = 2;

               view.setLayoutParams(tableParamsTopNews);

                mHolder.layou1.setOrientation(LinearLayout.VERTICAL);
                linearLayoutRowParams.width = 0;
                linearLayoutRowParams.weight = 2;
                mHolder.layou1.setLayoutParams(linearLayoutRowParams);

                sourceRowParams.height = 0;
                sourceRowParams.weight = 1;
                mHolder.source.setLayoutParams(sourceRowParams);
                mHolder.source.setText(mNewsItems.get(position).getPublisher_name());
                mHolder.source.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));

                titleRowParams.height = 0;
                titleRowParams.weight = 3;
                titleRowParams.rightMargin = 20;
                mHolder.title.setTextSize(17);
                mHolder.title.setLayoutParams(titleRowParams);
                mHolder.title.setLines(3);
                mHolder.title.setEllipsize(TextUtils.TruncateAt.END);

                descRowParams.height = 0;
                descRowParams.weight = 1;
                descRowParams.bottomMargin = 10;
                mHolder.desc.setLayoutParams(descRowParams);

                imageRowParams.width = 0;
                imageRowParams.height = 300;
                mHolder.image.setMinimumHeight(200);
                imageRowParams.weight = 1;
                imageRowParams.setMargins(0, 0, 0, 30);
                mHolder.image.setScaleType(ImageView.ScaleType.CENTER);
                mHolder.image.setImageResource(R.drawable.img1);
                mHolder.image.setLayoutParams(imageRowParams);

                Log.d(TAG, "count after:" + mHolder.layoutRowPhotos.getChildCount());
                break;
            default:
                mHolder.image.setImageResource(R.drawable.img2);
                mHolder.title.setText(mNewsItems.get(position).getTitle());
                mHolder.desc.setText(mNewsItems.get(position).getDescription());

                mHolder.layout.addView(mHolder.image);
                mHolder.layout.addView(mHolder.title);
                mHolder.layout.addView(mHolder.desc);
                break;

        }

        mHolder.title.setTextColor(mActivity.getResources().getColor(R.color.news_text_color));
        mHolder.desc.setTextColor(mActivity.getResources().getColor(R.color.news_time_color));

        // this section is common to all cases
        mHolder.title.setText(mNewsItems.get(position).getTitle());
        mHolder.desc.setText(mNewsItems.get(position).getPublish_time());

//        PrivateCDNHelper privateHelper = new PrivateCDNHelper(image);
//        privateHelper.setPaddedImage(image, mNewsItems.get(position).getPadded_img());
//  to add when there is an image      image.setImageResource(R.drawable.pomegranate);

        mHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click itemid:" + (mNewsItems.get(position).getUrl()));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNewsItems.get(position).getUrl()));
                mActivity.startActivity(browserIntent);
            }
        });


    }

    private LayerDrawable makeRound(int type) {

        float[] outerRadii = new float[]{15, 15, 15, 15, 15, 15, 15, 15};
        float[] innerRadii = new float[]{15, 15, 15, 15, 15, 15, 15, 15};

        ShapeDrawable backgroundShape = null;
        new ShapeDrawable(new RoundRectShape(
                outerRadii,
                null,
                innerRadii
        ));

        if (type == BUTTON_LAYOUT) {
            outerRadii = new float[]{75, 75, 75, 75, 75, 75, 75, 75};
            innerRadii = new float[]{75, 75, 75, 75, 75, 75, 75, 75};
            backgroundShape = new ShapeDrawable(new RoundRectShape(
                    outerRadii,
                    null,
                    innerRadii
            ));
            backgroundShape.getPaint().setColor(mActivity.getResources().getColor(R.color.turn_on_button)); 
            backgroundShape.getPaint().setStyle(Paint.Style.FILL);
            backgroundShape.getPaint().setAntiAlias(true);
            backgroundShape.setPadding(50, 20, 50, 20);

        } else if (type == CARD_LAYOUT) {
            backgroundShape = new ShapeDrawable(new RoundRectShape(
                    outerRadii,
                    null,
                    innerRadii
            ));
        }

        Drawable[] drawables = new Drawable[]{
                backgroundShape
        };

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        return layerDrawable;
    }

    public NewsItem getItem(int id) {
        return mNewsItems.get(id);
    }

    public interface RecycleItemClickListener {
        void onItemClick(View view, int position);

        void onOptInClick(View view);

        void onCloseClick(View view);
    }

}

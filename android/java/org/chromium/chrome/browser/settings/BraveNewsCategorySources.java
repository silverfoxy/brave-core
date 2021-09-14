/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import org.chromium.chrome.browser.settings.SearchPreference;

import org.chromium.base.ContextUtils;
import org.chromium.base.Log;
import org.chromium.brave_news.mojom.Publisher;
import org.chromium.brave_news.mojom.BraveNewsController;
import org.chromium.chrome.browser.brave_news.BraveNewsControllerFactory;
import org.chromium.chrome.browser.brave_news.models.NewsItem;
import org.chromium.chrome.browser.brave_news.models.PublisherCategory;
import org.chromium.mojo.bindings.ConnectionErrorHandler;
import org.chromium.mojo.system.MojoException;
import java.util.Map;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.chromium.chrome.R;

public class BraveNewsCategorySources extends PreferenceFragmentCompat implements 
    ConnectionErrorHandler  {

    private PreferenceManager preferenceManager;
    private BraveNewsController mBraveNewsController;
    private String categoryArg;

    private HashMap <String, List<Publisher>> categsPublishers;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("bn", "getPreferenceScreen().getKey():" + getPreferenceScreen().getKey());
        Log.d("bn", "getPreferenceScreen().getKey():" + getPreferenceScreen().getTitle());
        Bundle args = getArguments();
        categoryArg = args.getString("category");
        Log.d("bn", "getPreferenceScreen() category:" + categoryArg);
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        InitBraveNewsController();
        addPreferencesFromResource(R.xml.brave_news_sources_default);
        Log.d("bn", "getPreferenceScreen().getKey():" + getPreferenceScreen().getKey());
        getActivity().setTitle(categoryArg);

        categsPublishers = new HashMap<>();
        preferenceManager =  getPreferenceManager();
        mBraveNewsController.getPublishers((publishers) -> {
            Log.d("bn", "getfeed publishers: " + publishers);
            for (Map.Entry<String,Publisher> entry : publishers.entrySet()) {
                String key = entry.getKey();
                Log.d("bn", "category key:"+ key+ " " + categoryArg);

                    Publisher publisher = entry.getValue();
                    if (publisher.categoryName.toLowerCase().equals(categoryArg.toLowerCase())){
                        categsPublishers.computeIfAbsent(publisher.categoryName, k ->new ArrayList<>()).add(publisher);
                    }
            }

            addCategs(categsPublishers);
        });

    }

    @Override
    public void onConnectionError(MojoException e) {
        mBraveNewsController = null;
        InitBraveNewsController();
    }

    private void InitBraveNewsController() {
        if (mBraveNewsController != null) {
            return;
        }

        mBraveNewsController =
                BraveNewsControllerFactory.getInstance().getBraveNewsController(this);
    }

    private void addCategs(HashMap <String, List<Publisher>> publisherCategories){
        // Log.d("bn", "getfeed addCategs: " + publisherCategories);
        PreferenceScreen sourcesScreen = getPreferenceScreen();

        SearchPreference search = new SearchPreference(ContextUtils.getApplicationContext()); 
        search.setKey("search_pref");


        sourcesScreen.addPreference(search); 
        
        for(Map.Entry<String,List<Publisher>> map : publisherCategories.entrySet()){
            String category = map.getKey();
            Log.d("bn", "getfeed addCategs: " + category);
            if (category.toLowerCase().equals(categoryArg.toLowerCase())){

                List<Publisher> publishers = map.getValue();

                //preferenceManager.createPreferenceScreen(ContextUtils.getApplicationContext());
                // sourcesScreen.setLayoutResource(R.layout.custom_brave_news_sources_preference);

                search.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        for (Publisher publisher : publishers){
                            SwitchPreference publisherSource = preferenceManager.findPreference(publisher.publisherName);
                            if (publisher.publisherName.toLowerCase().contains((String) newValue)){
                                publisherSource.setVisible(true);
                            } else {
                                publisherSource.setVisible(false);
                            }
                        }
                        return false;
                    }
                });

                for (Publisher publisher : publishers){

                    SwitchPreference source = new SwitchPreference(ContextUtils.getApplicationContext()); 
                    source.setTitle(publisher.publisherName);
                    source.setKey(publisher.publisherName);
                    source.setChecked(publisher.isEnabled); 
                    sourcesScreen.addPreference(source); 
                }
            }
            
            // setPreferenceScreen(sourcesScreen);

            // mainScreen.addPreference(source);
        }
    }



    
}

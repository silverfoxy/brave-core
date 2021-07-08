
/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.settings;

import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceCategory;
import androidx.preference.EditTextPreference;
import androidx.preference.CheckBoxPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;


import org.chromium.base.ContextUtils;
import org.chromium.chrome.browser.BraveLaunchIntentDispatcher;
import org.chromium.chrome.browser.preferences.BravePreferenceKeys;
import org.chromium.chrome.browser.settings.BravePreferenceFragment;
import org.chromium.chrome.browser.settings.BraveAddNewsSources;
import org.chromium.chrome.R;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.settings.ChromeBasePreference;
import org.chromium.components.browser_ui.settings.SettingsUtils;
import java.util.HashMap;
import org.chromium.base.Log;

public class BraveNewsPreferences extends BravePreferenceFragment
        implements Preference.OnPreferenceChangeListener {


    private static final String PREF_TURN_ON_NEWS = "turn_on_news";
    private static final String PREF_SHOW_NEWS = "show_news";
    private static final String PREF_SOURCES_SECTION = "your_sources_section";
    private static final String PREF_ADD_SOURCES = "add_source_news";

    private ChromeSwitchPreference turnOnNews;
    private ChromeSwitchPreference showNews;
    private PreferenceCategory yourSources;
    private ChromeBasePreference addSource;
    private EditText mEditText;
    private PreferenceScreen mainScreen;

    private final HashMap<String, Preference> mRemovedPreferences = new HashMap<>();


    public static int getPreferenceSummary() {
        return BraveLaunchIntentDispatcher.useCustomTabs() ? R.string.text_on : R.string.text_off;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        
        SettingsUtils.addPreferencesFromResource(this, R.xml.brave_news_preferences);

        turnOnNews = (ChromeSwitchPreference) findPreference(PREF_TURN_ON_NEWS);
        showNews = (ChromeSwitchPreference) findPreference(PREF_SHOW_NEWS);
        yourSources = (PreferenceCategory) findPreference(PREF_SOURCES_SECTION);
        addSource = (ChromeBasePreference) findPreference(PREF_ADD_SOURCES);

        turnOnNews.setOnPreferenceChangeListener(this);
        showNews.setOnPreferenceChangeListener(this);
        addSource.setOnPreferenceChangeListener(this);
    }

    // @Override
    // public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
    //         @Nullable Bundle savedInstanceState) {
    // }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.brave_news_title);

        mainScreen = getPreferenceManager().getPreferenceScreen();
        
        SharedPreferences sharedPreferences = ContextUtils.getAppSharedPreferences();
        boolean isNewsOn = sharedPreferences.getBoolean(PREF_TURN_ON_NEWS, false);

        if (!isNewsOn){
            turnOnNews.setChecked(false);
            showNews.setVisible(false);
            yourSources.setVisible(false);
            addSource.setVisible(false);
        } else {
            turnOnNews.setChecked(true);
            showNews.setVisible(true);
            if (sharedPreferences.getBoolean(PREF_SHOW_NEWS, false)){
                showNews.setChecked(true);
            } else {
                showNews.setChecked(false);
            }
            
            yourSources.setVisible(true);
            addSource.setVisible(true);
        }
	}
    
   @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        SharedPreferences.Editor sharedPreferencesEditor =
                ContextUtils.getAppSharedPreferences().edit();
        Log.d("BN", "pref change key:"+key);
        if (PREF_TURN_ON_NEWS.equals(key)) {
            sharedPreferencesEditor.putBoolean(PREF_TURN_ON_NEWS, (boolean) newValue);
            if ((boolean) newValue) {
                Log.d("BN", "pref true add stuff");
            
                showNews.setVisible(true);
                showNews.setChecked(true);
                sharedPreferencesEditor.putBoolean(PREF_SHOW_NEWS, true);
                yourSources.setVisible(true);
                addSource.setVisible(true);
            } else {

                showNews.setVisible(false);
                yourSources.setVisible(false);
                addSource.setVisible(false);
            }
        } else if (PREF_SHOW_NEWS.equals(key)) {
            sharedPreferencesEditor.putBoolean(PREF_SHOW_NEWS,  (boolean) newValue);
            if ((boolean) newValue) {
            } else {

            }

        // } else if (PREF_ADD_SOURCES.equals(key)) {
        //     Log.d("bn", "edittext:" + mEditText);

        //     PreferenceManager manager =  getPreferenceManager();
        //     PreferenceScreen sourcesScreen = manager.createPreferenceScreen(ContextUtils.getApplicationContext());
        //     SwitchPreference source = new SwitchPreference(ContextUtils.getApplicationContext());
        //     source.setTitle((String) newValue);
        //     sourcesScreen.addPreference(source);

        //     setPreferenceScreen(sourcesScreen);
        //     return false;


            // getPreferenceScreen().setOrderingAsAdded(true);
            // SwitchPreference source = new SwitchPreference(ContextUtils.getApplicationContext());
            // source.setTitle((String) newValue);
            // source.setChecked(true);
            // int buttonOrder = addSource.getOrder();
            // source.setOrder(++buttonOrder);
            // getPreferenceScreen().addPreference(source); 
        } else  if (PREF_ADD_SOURCES.equals(key)) {
            Log.d("bn", "edittext:" + mEditText);



            // PreferenceManager manager =  getPreferenceManager();
            // PreferenceScreen sourcesScreen = manager.createPreferenceScreen(ContextUtils.getApplicationContext());
            // CheckBoxPreference source = new CheckBoxPreference(ContextUtils.getApplicationContext());
            // source.setTitle((String) newValue);
            // source.setChecked(true);
            // sourcesScreen.addPreference(source);

            // Preference button = new Preference(ContextUtils.getApplicationContext());
            // button.setTitle("Add");
            // button.setKey("add_news_source");
            // button.setSummary("Cool button stuff");
            // button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            //     @Override
            //     public boolean onPreferenceClick(Preference preference) { 
            //         Log.d("bn", "adding sources...");
                    
            //         setPreferenceScreen(mainScreen);
            //          // SettingsUtils.addPreferencesFromResource(this, R.xml.brave_news_preferences);
            //         // inflateFromResource(ContextUtils.getApplicationContext(), R.layout.brave_news_preferences, getPreferenceScreen());
            //         return true;
            //     }
            // });
            // sourcesScreen.addPreference(button);

            // setPreferenceScreen(sourcesScreen);
            // getPreferenceManager().setOnNavigateToScreenListener(new PreferenceManager.OnNavigateToScreenListener() {
            //     @Override
            //     public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
            //         Log.d("bn", "onNavigateToScreen..." + preferenceScreen.getTitle() + " parent:" + preferenceScreen.getParent());
            //     }
            // });

            return true;
        }
        return true;
    }


    private void removePreferenceIfPresent(String key) {
        Preference preference = getPreferenceScreen().findPreference(key);
        if (preference != null) {
            getPreferenceScreen().removePreference(preference);
            mRemovedPreferences.put(preference.getKey(), preference);
        }
    }


       /**
     *  We need to override it to avoid NullPointerException in Chromium's child classes
     */
    @Override
    public <T extends Preference> T findPreference(CharSequence key) {
        T result = super.findPreference(key);
        if (result == null) {
            result = (T) mRemovedPreferences.get((String) key);
        }
        return result;
    }

}


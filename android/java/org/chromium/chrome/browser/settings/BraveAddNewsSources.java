
/* Copyright (c) 2020 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.chromium.chrome.browser.settings;

import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import androidx.annotation.NonNull;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import java.util.ArrayList;


import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import org.chromium.base.Log;
import org.chromium.chrome.browser.app.BraveActivity;

import org.chromium.base.ContextUtils;
import org.chromium.chrome.browser.BraveLaunchIntentDispatcher;
import org.chromium.chrome.browser.preferences.BravePreferenceKeys;
import org.chromium.chrome.browser.settings.BravePreferenceFragment;
import org.chromium.chrome.R;
import org.chromium.components.browser_ui.settings.ChromeSwitchPreference;
import org.chromium.components.browser_ui.settings.ChromeBaseCheckBoxPreference;
import org.chromium.components.browser_ui.settings.SettingsUtils;

public class BraveAddNewsSources extends BravePreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_ADD_SOURCES = "news_source_1";
    private EditTextPreference addSource;
    private EditText mEditText;
    private PreferenceScreen mainScreen;

    public static int getPreferenceSummary() {
        return BraveLaunchIntentDispatcher.useCustomTabs() ? R.string.text_on : R.string.text_off;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.news_add_source);
        SettingsUtils.addPreferencesFromResource(this, R.xml.brave_news_sources);
        findPreference(PREF_ADD_SOURCES).setOnPreferenceChangeListener(this);

        mainScreen = getPreferenceManager().getPreferenceScreen();

        addSource = (EditTextPreference) findPreference(PREF_ADD_SOURCES);
        addSource.setPositiveButtonText(R.string.search_title);
	}
    

    // @Override
    // public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    //     getActivity().setTitle(R.string.prefs_use_custom_tabs);
    //     SettingsUtils.addPreferencesFromResource(this, R.xml.use_custom_tabs_brave_preference);

    //     ChromeSwitchPreference pref = (ChromeSwitchPreference) findPreference(
    //             BravePreferenceKeys.BRAVE_USE_CUSTOM_TABS);
    //     pref.setChecked(BraveLaunchIntentDispatcher.useCustomTabs());
    //     pref.setOnPreferenceChangeListener(this); formulaires.chumontreal.qc.ca
    // }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();
        
        if (PREF_ADD_SOURCES.equals(key)) {
            Log.d("bn", "edittext:" + mEditText);

            PreferenceManager manager =  getPreferenceManager();
            PreferenceScreen sourcesScreen = manager.createPreferenceScreen(ContextUtils.getApplicationContext());
            sourcesScreen.setTitle((String) newValue);
            //fetch results from API
            // populate checkboxes
            CheckBoxPreference source1 = new CheckBoxPreference(ContextUtils.getApplicationContext());
            source1.setTitle((String) newValue + " 1");
            source1.setChecked(true);
            sourcesScreen.addPreference(source1);            


            CheckBoxPreference source2 = new CheckBoxPreference(ContextUtils.getApplicationContext());
            source2.setTitle((String) newValue + " 2");
            source2.setChecked(true);
            sourcesScreen.addPreference(source2);

            //end fetch. finish the layout

            Preference button = new Preference(ContextUtils.getApplicationContext());
            button.setTitle("Add");
            button.setKey("add_news_source");
            button.setSummary("Cool button stuff");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) { 
                    Log.d("bn", "adding sources...");


                    ArrayList<Preference> list = getPreferenceList(sourcesScreen, new ArrayList<Preference>());
                    for (Preference p : list) {
                        if(p instanceof CheckBoxPreference) {
                            CheckBoxPreference pref = (CheckBoxPreference) p;
                            // Log.d("bn", "CheckBoxPreference: ");
                            if (pref.isChecked()){
                                Log.d("bn", "preference: " + p.getTitle());
                            }
                        }
                    }
                    // setPreferenceScreen(mainScreen);
                    setPreferencesFromResource(R.xml.brave_news_preferences, null);
                     // SettingsUtils.addPreferencesFromResource(this, R.xml.brave_news_preferences);
                    // inflateFromResource(ContextUtils.getApplicationContext(), R.layout.brave_news_preferences, getPreferenceScreen());
                    return true;
                }
            });
            sourcesScreen.addPreference(button);
            
            setPreferenceScreen(sourcesScreen);
            return true;
        }
        return true;
    }


    private ArrayList<Preference> getPreferenceList(Preference p, ArrayList<Preference> list) {
        if( p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            for(int i = 0; i < pCount; i++) {
                Log.d("bn", "in for:"+pGroup.getPreference(i).getTitle());
                getPreferenceList(pGroup.getPreference(i), list); // recursive call
            }
        } else {
            Log.d("bn", "in add p:"+p.getTitle());
            list.add(p);
        }
        return list;
    }
}


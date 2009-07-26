/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.norwegian;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.text.AutoText;

import android.preference.EditTextPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.ListPreference;
import android.content.res.Resources;
import android.content.pm.PackageManager.NameNotFoundException;
import java.lang.CharSequence;
import java.util.ArrayList;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.text.Html;

public class NorwegianIMESettings extends PreferenceActivity {
    
    private static final String HELP = "help";
    private static final int DIALOG_HELP = 0;
    private static final String QUICK_FIXES_KEY = "quick_fixes";
    private static final String SHOW_SUGGESTIONS_KEY = "show_suggestions";
    private static final String PREDICTION_SETTINGS_KEY = "prediction_settings";
    private static final String VIBRATE_DURATION = "vibration_duration";
    //private static final String DICTIONARY_MANUALLY = "dictionary_manually";
    private static final String DICTIONARY = "dictionary";
    
    private Preference mHelp;
    private CheckBoxPreference mQuickFixes;
    private CheckBoxPreference mShowSuggestions;
    private EditTextPreference mVibrateDuration;
    //private CheckBoxPreference mDictionaryManually;
    private ListPreference mDictionary;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs);
        
        mHelp = findPreference(HELP);
        mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
        mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
        
        mVibrateDuration = (EditTextPreference) findPreference(VIBRATE_DURATION);
        mVibrateDuration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {   
            public boolean onPreferenceChange(Preference preference, Object newValue) {   
                try {
                    Integer.parseInt(newValue.toString());
                } catch(NumberFormatException nfe) {
                    return false;
                }
                return true;
            }   
        });
        
        //mDictionaryManually = (CheckBoxPreference) findPreference(DICTIONARY_MANUALLY);
        mDictionary = (ListPreference) findPreference(DICTIONARY);
        entries = mDictionary.getEntries();
        entryValues = mDictionary.getEntryValues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int autoTextSize = AutoText.getSize(getListView());
        if (autoTextSize < 1) {
            ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
                .removePreference(mQuickFixes);
        } else {
            //mShowSuggestions.setDependency(QUICK_FIXES_KEY);
        }
        
        String[] pkgNames = { "com.android.inputmethod.norwegian.norwegiandictionary", "com.android.inputmethod.norwegian.danishdictionary", "com.android.inputmethod.latin", "com.android.inputmethod.norwegian.swedishdictionary" };
        ArrayList<Integer> foundList = new ArrayList<Integer>();
        for( int i = 0; i < pkgNames.length; i++) {
            Resources res;
            Boolean found = true;
            try {
                res = getPackageManager().getResourcesForApplication(pkgNames[i]);
            } catch(NameNotFoundException notFound) {
                found = false;
            }
            if(found) foundList.add(i);
        }
        CharSequence[] newEntries = new CharSequence[foundList.size()];
        CharSequence[] newEntryValues = new CharSequence[foundList.size()];
        for(int i = 0; i < foundList.size(); i++) {
            for(int j = 0; j < entries.length; j++) {
                if(Integer.toString(foundList.get(i)).charAt(0) == entryValues[j].charAt(0)) {
                    newEntries[i] = entries[j];
                    newEntryValues[i] = entryValues[j];
                }
            }
        }
        mDictionary.setEntries(newEntries);
        mDictionary.setEntryValues(newEntryValues);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d;
        switch (id) {
        case DIALOG_HELP:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.help)
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setPositiveButton("Homepage", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                              Uri.parse("http://code.google.com/p/scandinavian-keyboard/")));
                        }
                    })
                    .setNeutralButton("Donate", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                              Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6117255")));
                        }
                    })
                    .setNegativeButton("Close", null);
                    //.setMessage(R.string.help_text);
            View v = LayoutInflater.from(this).inflate(R.layout.dialog, null);
		    TextView text = (TextView) v.findViewById(R.id.dialogText);
		    //text.setText(getString(R.string.help_text, getVersion()));
		    String resultsTextFormat = getString(R.string.help_text);
            String resultsText = String.format(resultsTextFormat);
            CharSequence styledResults = Html.fromHtml(resultsText);
		    text.setText(styledResults);
		    text.setTextColor(text.getCurrentTextColor());
		    text.setMovementMethod(LinkMovementMethod.getInstance());
		    builder.setView(v);
            d = builder.create();
            break;
        default:
            d = null;
        }

        return d;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHelp) {
            showDialog(DIALOG_HELP);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) { }
        return "";
    }
}

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

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.ViewGroup;


public class NorwegianIMESettings extends PreferenceActivity {
    
    private static final String HELP = "help";
    private static final int DIALOG_HELP = 0;
    private static final String QUICK_FIXES_KEY = "quick_fixes";
    private static final String SHOW_SUGGESTIONS_KEY = "show_suggestions";
    private static final String PREDICTION_SETTINGS_KEY = "prediction_settings";
    private static final String VIBRATE_DURATION = "vibrate_duration";
    private static final int DIALOG_VIBRATE_DURATION = 1;
    private static final String KEYBOARD_LAYOUT = "keyboard_layout";
    private static final String DICTIONARY_MANUALLY = "dictionary_manually";
    private static final String DICTIONARY = "dictionary";
    private static final String DICTIONARY_MARKET = "dictionary_market";
    
    private Preference mHelp;
    private CheckBoxPreference mQuickFixes;
    private CheckBoxPreference mShowSuggestions;
    private Preference mVibrateDuration;
    private ListPreference mKeyboardLayout;
    private CheckBoxPreference mDictionaryManually;
    private ListPreference mDictionary;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private Preference mDictionaryMarket;
    
    private LinearLayout vibrateDurationLayout;
    private SharedPreferences sp;
    private int vibrationDurationValue;
    private TextView vibrateDurationValueText;
    private SeekBar vibrateDurationSeekBar;
    private TextView vibrateWarning;
    private boolean vibrateWarningVisible;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs);
        
        mHelp = findPreference(HELP);
        mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
        mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
        mVibrateDuration = findPreference(VIBRATE_DURATION);
        
        mKeyboardLayout = (ListPreference) findPreference(KEYBOARD_LAYOUT);
        mDictionaryManually = (CheckBoxPreference) findPreference(DICTIONARY_MANUALLY);
        mDictionary = (ListPreference) findPreference(DICTIONARY);
        entries = mDictionary.getEntries();
        entryValues = mDictionary.getEntryValues();
        mDictionaryMarket = findPreference(DICTIONARY_MARKET);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        mKeyboardLayout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean dictionaryManually = sp.getBoolean(DICTIONARY_MANUALLY, false);
                int dictionary = Integer.parseInt(sp.getString(DICTIONARY, "0"));
                addRemoveQuickFixes(Integer.parseInt(newValue.toString()), dictionaryManually, dictionary);
                return true;
            }
        });
        mDictionaryManually.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int keyboardLayout = Integer.parseInt(sp.getString(KEYBOARD_LAYOUT, "0"));
                int dictionary = Integer.parseInt(sp.getString(DICTIONARY, "0"));
                addRemoveQuickFixes(keyboardLayout, (Boolean)newValue, dictionary);
                return true;
            }
        });
        mDictionary.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int keyboardLayout = Integer.parseInt(sp.getString(KEYBOARD_LAYOUT, "0"));
                boolean dictionaryManually = sp.getBoolean(DICTIONARY_MANUALLY, false);
                addRemoveQuickFixes(keyboardLayout, dictionaryManually, Integer.parseInt(newValue.toString()));
                return true;
            }
        });
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
        
        int keyboardLayout = Integer.parseInt(sp.getString(KEYBOARD_LAYOUT, "0"));
        boolean dictionaryManually = sp.getBoolean(DICTIONARY_MANUALLY, false);
        int dictionary = Integer.parseInt(sp.getString(DICTIONARY, "0"));
        addRemoveQuickFixes(keyboardLayout, dictionaryManually, dictionary);
        
        String[] pkgNames = { "com.android.inputmethod.norwegian.norwegiandictionary", "com.android.inputmethod.norwegian.danishdictionary", "com.android.inputmethod.latin", "com.android.inputmethod.norwegian.swedishdictionary", "com.android.inputmethod.norwegian.finnishdictionary" };
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
            AlertDialog.Builder builderHelp = new AlertDialog.Builder(this);
            builderHelp.setTitle(R.string.help)
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
		    builderHelp.setView(v);
            d = builderHelp.create();
            break;
        case DIALOG_VIBRATE_DURATION:
            AlertDialog.Builder builderVibrateDuration = new AlertDialog.Builder(this);
            builderVibrateDuration.setTitle(R.string.vibrate_duration)
                                  .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          SharedPreferences.Editor editor = sp.edit();
                                          editor.putInt(VIBRATE_DURATION, vibrateDurationSeekBar.getProgress());
                                          editor.commit();
                                          vibrationDurationValue = vibrateDurationSeekBar.getProgress();
                                      }
                                  })
                                  .setNegativeButton("Cancel", null);
            
            vibrateDurationLayout = new LinearLayout(this);
            vibrateDurationLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            vibrateDurationLayout.setPadding(20, 20, 20, 20);
            vibrateDurationLayout.setOrientation(LinearLayout.VERTICAL);
            
            sp = PreferenceManager.getDefaultSharedPreferences(this);
            vibrationDurationValue = sp.getInt(VIBRATE_DURATION, getResources().getInteger(R.integer.vibrate_duration_ms));

            vibrateDurationValueText = new TextView(this);
            vibrateDurationValueText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            vibrateDurationValueText.setText("Duration: " + Integer.toString(vibrationDurationValue) + " ms");
            
            vibrateDurationSeekBar = new SeekBar(this);
            vibrateDurationSeekBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            vibrateDurationSeekBar.setPadding(0, 10, 0, 10);
            vibrateDurationSeekBar.setMax(100);
            vibrateDurationSeekBar.setProgress(vibrationDurationValue);
            vibrateDurationSeekBar.setKeyProgressIncrement(1);
            vibrateDurationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    vibrateDurationValueText.setText("Duration: " + Integer.toString(progress) + " ms");
                    if(progress < 30 && !vibrateWarningVisible) {
                        vibrateWarning.setText(R.string.vibrate_warning);
                        vibrateWarningVisible = true;
                    }
                    else if(progress >= 30 && vibrateWarningVisible) {
                        if(getWindow().getWindowManager().getDefaultDisplay().getWidth() < 350)
                            vibrateWarning.setText(" \n ");
                        else
                            vibrateWarning.setText(" ");
                        vibrateWarningVisible = false;
                    }
                }
                public void onStartTrackingTouch(SeekBar seekBar) { }
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            
            vibrateWarning = new TextView(this);
            vibrateWarning.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            vibrateWarning.setText(R.string.vibrate_warning);
            
            vibrateDurationLayout.addView(vibrateDurationValueText);
            vibrateDurationLayout.addView(vibrateDurationSeekBar);
            if(vibrationDurationValue < 30) {
                vibrateDurationLayout.addView(vibrateWarning);
                vibrateWarningVisible = true;
            }
            
            builderVibrateDuration.setView(vibrateDurationLayout);
            d = builderVibrateDuration.create();
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
        } else if (preference == mVibrateDuration) {
            if(vibrateDurationSeekBar != null) vibrateDurationSeekBar.setProgress(vibrationDurationValue);
            showDialog(DIALOG_VIBRATE_DURATION);
        } else if (preference == mDictionaryMarket) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                              Uri.parse("market://search?q=scandinavian keyboard")));
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) { }
        return "";
    }

    private void addRemoveQuickFixes(int keyboardLayout, boolean dictionaryManually, int dictionary) {
        if((keyboardLayout != 2 && !dictionaryManually) || (dictionary != 2 && dictionaryManually))
        ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
            .removePreference(mQuickFixes);
        else
        ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
            .addPreference(mQuickFixes);
    }
}

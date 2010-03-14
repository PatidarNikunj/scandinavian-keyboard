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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.AutoText;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class NorwegianIMESettings extends PreferenceActivity {
    
    private static final String HELP = "help";
    private static final int DIALOG_HELP = 0;
    private static final String QUICK_FIXES_KEY = "quick_fixes";
    //private static final String SHOW_SUGGESTIONS_KEY = "show_suggestions";
    private static final String PREDICTION_SETTINGS_KEY = "prediction_settings";
    private static final String VIBRATE_OPTIONS = "vibrate_options";
    private static final String VIBRATE_ENABLE = "vibrate_enable";
    private static final String VIBRATE_DURATION = "vibrate_duration";
    private static final String VIBRATE_BUG_FIX = "vibrate_bug_fix";
    private static final int DIALOG_VIBRATE_OPTIONS = 1;
    private static final String KEYBOARD_LAYOUT = "keyboard_layout";
    private static final String DICTIONARY_MANUALLY = "dictionary_manually";
    private static final String DICTIONARY = "dictionary";
    private static final String DICTIONARY_MARKET = "dictionary_market";
    
    private static final String SWIPE_SETTINGS = "swipe_settings";
    private static final String SWIPE_UP = "swipe_up";
    private static final String SWIPE_DOWN = "swipe_down";
    private static final String SWIPE_LEFT = "swipe_left";
    private static final String SWIPE_RIGHT = "swipe_right";
    private static final String SWIPE_KEYBOARD_LAYOUT = "swipe_keyboard_layout";
    private static final String SWIPE_DICTIONARY = "swipe_dictionary";
    
    private static final String AUTO_DICTIONARY_OPTIONS = "auto_dictionary_options";
    private static final String AUTO_DICTIONARY_ENABLE = "auto_dictionary_enable";
    private static final String AUTO_DICTIONARY_LIMIT = "auto_dictionary_limit";
    private static final int DIALOG_AUTO_DICTIONARY_OPTIONS = 2;
    
    private Preference mHelp;
    private CheckBoxPreference mQuickFixes;
    //private CheckBoxPreference mShowSuggestions;
    private Preference mVibrateOptions;
    private ListPreference mKeyboardLayout;
    private CheckBoxPreference mDictionaryManually;
    private ListPreference mDictionary;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private Preference mDictionaryMarket;
    
    private ListPreference[] mSwipe = new ListPreference[4];
    private ListPreferenceMultiSelect mSwipeKeyboardLayout;
    private ListPreferenceMultiSelect mSwipeDictionary;
    
    private Preference mAutoDictionaryOptions;
    
    private SharedPreferences sp;
//    private int vibrationDurationValue;
//    private TextView vibrateDurationValueText;
//    private SeekBar vibrateDurationSeekBar;
//    private TextView vibrateWarning;
//    private boolean vibrateWarningVisible;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs);
        
        mHelp = findPreference(HELP);
        mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
        //mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
        mVibrateOptions = findPreference(VIBRATE_OPTIONS);
        
        mKeyboardLayout = (ListPreference) findPreference(KEYBOARD_LAYOUT);
        mDictionaryManually = (CheckBoxPreference) findPreference(DICTIONARY_MANUALLY);
        mDictionary = (ListPreference) findPreference(DICTIONARY);
        entries = mDictionary.getEntries();
        entryValues = mDictionary.getEntryValues();
        mDictionaryMarket = findPreference(DICTIONARY_MARKET);
        
        mSwipe[0] = (ListPreference) findPreference(SWIPE_UP);
        mSwipe[1] = (ListPreference) findPreference(SWIPE_DOWN);
        mSwipe[2] = (ListPreference) findPreference(SWIPE_LEFT);
        mSwipe[3] = (ListPreference) findPreference(SWIPE_RIGHT);
        mSwipeKeyboardLayout = (ListPreferenceMultiSelect) findPreference(SWIPE_KEYBOARD_LAYOUT);
        mSwipeDictionary = (ListPreferenceMultiSelect) findPreference(SWIPE_DICTIONARY);
        
        mAutoDictionaryOptions = findPreference(AUTO_DICTIONARY_OPTIONS);

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
        
        for(ListPreference swipe : mSwipe) {
        	swipe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {				
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					((ListPreference) preference).setValue((String)newValue);
					addRemoveSwipe();
					return true;
				}
			});
        }
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
            Boolean found = true;
            try {
                getPackageManager().getResourcesForApplication(pkgNames[i]);
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
        mSwipeKeyboardLayout.checkAllIfValueIsAll();
        mSwipeDictionary.setEntries(newEntries);
        mSwipeDictionary.setEntryValues(newEntryValues);
        mSwipeDictionary.checkAllIfValueIsAll();
        
        addRemoveSwipe();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = null;
        switch (id) {
        case DIALOG_HELP:
            AlertDialog.Builder builderHelp = new AlertDialog.Builder(this);
            builderHelp.setTitle(R.string.help)
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setPositiveButton(R.string.homepage, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                              Uri.parse("http://code.google.com/p/scandinavian-keyboard/")));
                        }
                    })
                    .setNeutralButton(R.string.donate, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                              Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6117255")));
                        }
                    })
                    .setNegativeButton(R.string.close, null);
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
            
        case DIALOG_VIBRATE_OPTIONS:
        	View vibrateView = LayoutInflater.from(this).inflate(R.layout.dialog_vibrate, null);
        	final CheckBox vibrateEnable = (CheckBox) vibrateView.findViewById(R.id.vibrate_enable);
        	final TextView vibrateDurationText = (TextView) vibrateView.findViewById(R.id.vibrate_duration_text);
        	final SeekBar vibrateDuration = (SeekBar) vibrateView.findViewById(R.id.vibrate_duration);
        	final CheckBox vibrateBugFix = (CheckBox) vibrateView.findViewById(R.id.vibrate_bug_fix);
            
        	vibrateEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					vibrateDuration.setEnabled(isChecked);
					vibrateBugFix.setEnabled(isChecked);
				}
			});
			
            vibrateDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    vibrateDurationText.setText(getResources().getString(R.string.vibrate_duration) + " " + Integer.toString(progress) + " ms");
                }
                public void onStartTrackingTouch(SeekBar seekBar) { }
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            
            d = new AlertDialog.Builder(this)
                  .setTitle(R.string.vibrate_options)
                  .setView(vibrateView)
                  .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                          SharedPreferences.Editor editor = sp.edit();
                          editor.putBoolean(VIBRATE_ENABLE, vibrateEnable.isChecked());
                          editor.putInt(VIBRATE_DURATION, vibrateDuration.getProgress());
                          editor.putBoolean(VIBRATE_BUG_FIX, vibrateBugFix.isChecked());
                          editor.commit();
                      }
                  })
                  .setNegativeButton(android.R.string.cancel, null)
                  .create();
            break;
            
        case DIALOG_AUTO_DICTIONARY_OPTIONS:
           	View autoDictView = LayoutInflater.from(this).inflate(R.layout.dialog_auto_dictionary, null);
        	final CheckBox autoDictEnable = (CheckBox) autoDictView.findViewById(R.id.auto_dictionary_enable);
        	final EditText autoDictLimit = (EditText) autoDictView.findViewById(R.id.auto_dictionary_limit);
            
        	autoDictEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					autoDictLimit.setEnabled(isChecked);
					autoDictLimit.setClickable(isChecked);
				}
			});
        	
            d = new AlertDialog.Builder(this)
                  .setTitle(R.string.auto_dictionary_options)
                  .setView(autoDictView)
                  .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                          SharedPreferences.Editor editor = sp.edit();
                          editor.putBoolean(AUTO_DICTIONARY_ENABLE, autoDictEnable.isChecked());
                          editor.putString(AUTO_DICTIONARY_LIMIT, autoDictLimit.getText().toString());
                          editor.commit();
                      }
                  })
                  .setNegativeButton(android.R.string.cancel, null)
                  .create();
        	break;
        }
        return d;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
    	switch (id) {
		case DIALOG_VIBRATE_OPTIONS:
    		AlertDialog vibrateDialog = (AlertDialog) d;
    		CheckBox vibrateEnable = (CheckBox) vibrateDialog.findViewById(R.id.vibrate_enable);
        	TextView vibrateDurationText = (TextView) vibrateDialog.findViewById(R.id.vibrate_duration_text);
        	SeekBar vibrateDuration = (SeekBar) vibrateDialog.findViewById(R.id.vibrate_duration);
        	CheckBox vibrateBugFix = (CheckBox) vibrateDialog.findViewById(R.id.vibrate_bug_fix);
        	
            sp = PreferenceManager.getDefaultSharedPreferences(this);
            boolean vibrationEnabled = sp.getBoolean(VIBRATE_ENABLE, true);
            int vibrationDurationValue = sp.getInt(VIBRATE_DURATION, getResources().getInteger(R.integer.vibrate_duration_ms));
        	
            vibrateEnable.setChecked(vibrationEnabled);
            vibrateDurationText.setText(getResources().getString(R.string.vibrate_duration) + " " + Integer.toString(vibrationDurationValue) + " ms");
        	vibrateDuration.setProgress(vibrationDurationValue);
        	vibrateBugFix.setChecked(sp.getBoolean(VIBRATE_BUG_FIX, false));
        	
        	if(!vibrationEnabled) {
        		vibrateDuration.setEnabled(false);
        		vibrateBugFix.setEnabled(false);
        	}
			break;

		case DIALOG_AUTO_DICTIONARY_OPTIONS:
    		AlertDialog autoDictDialog = (AlertDialog) d;
    		CheckBox autoDictEnable = (CheckBox) autoDictDialog.findViewById(R.id.auto_dictionary_enable);
        	EditText autoDictLimit = (EditText) autoDictDialog.findViewById(R.id.auto_dictionary_limit);
        	
            sp = PreferenceManager.getDefaultSharedPreferences(this);
            boolean autoDictEnabled = sp.getBoolean(AUTO_DICTIONARY_ENABLE, true);
            String autoDictLimitValue = sp.getString(AUTO_DICTIONARY_LIMIT, getResources().getString(R.string.auto_dictionary_limit_default));
        	
            autoDictEnable.setChecked(autoDictEnabled);
        	autoDictLimit.setText(autoDictLimitValue);
        	
        	if(!autoDictEnabled) {
        		autoDictLimit.setEnabled(false);
        	}
			break;
		}
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHelp) {
            showDialog(DIALOG_HELP);
        } else if (preference == mVibrateOptions) {
            showDialog(DIALOG_VIBRATE_OPTIONS);
        } else if (preference == mDictionaryMarket) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                              Uri.parse("market://search?q=scandinavian keyboard")));
        } else if (preference == mAutoDictionaryOptions) {
        	showDialog(DIALOG_AUTO_DICTIONARY_OPTIONS);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

//    private String getVersion() {
//        try {
//            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//        } catch (NameNotFoundException e) { }
//        return "";
//    }

    private void addRemoveQuickFixes(int keyboardLayout, boolean dictionaryManually, int dictionary) {
        if((keyboardLayout != 2 && !dictionaryManually) || (dictionary != 2 && dictionaryManually))
        ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
            .removePreference(mQuickFixes);
        else
        ((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
            .addPreference(mQuickFixes);
    }
    
    private void addRemoveSwipe() {
    	boolean keyboardLayoutSelected = false;
    	boolean dictionarySelected = false;
    	for(ListPreference swipe : mSwipe) {
    		if("7".equals(swipe.getValue()) || "8".equals(swipe.getValue()))
    			keyboardLayoutSelected = true;
    		if("9".equals(swipe.getValue()) || "10".equals(swipe.getValue()))
    			dictionarySelected = true;
    	}
    	if(keyboardLayoutSelected)
    		((PreferenceCategory) findPreference(SWIPE_SETTINGS)).addPreference(mSwipeKeyboardLayout);
    	else
    		((PreferenceCategory) findPreference(SWIPE_SETTINGS)).removePreference(mSwipeKeyboardLayout);
    	if(dictionarySelected) {
    		((PreferenceCategory) findPreference(SWIPE_SETTINGS)).addPreference(mSwipeDictionary);
    		mDictionaryManually.setChecked(true);
    	    SharedPreferences.Editor editor = sp.edit();
    	    editor.putBoolean(DICTIONARY_MANUALLY, true);
    	    editor.commit();
    	} else
    		((PreferenceCategory) findPreference(SWIPE_SETTINGS)).removePreference(mSwipeDictionary);
    }
}

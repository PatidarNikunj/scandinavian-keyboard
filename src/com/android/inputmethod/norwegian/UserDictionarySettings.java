/**
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.inputmethod.norwegian;

import java.io.File;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.inputmethod.norwegian.provider.UserDictionary;

public class UserDictionarySettings extends ListActivity {

    private static final String INSTANCE_KEY_DIALOG_EDITING_WORD = "DIALOG_EDITING_WORD";
    private static final String INSTANCE_KEY_ADDED_WORD = "DIALOG_ADDED_WORD";

    private static final String[] QUERY_PROJECTION = {
        UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY
    };
    
    // Either the locale is empty (means the word is applicable to all locales)
    // or the word equals our current locale
    private static final String QUERY_SELECTION = UserDictionary.Words.LOCALE + "=? OR "
            + UserDictionary.Words.LOCALE + " is null";

    private static final String DELETE_SELECTION = UserDictionary.Words.WORD + "=?";

    private static final String EXTRA_WORD = "word";
    
    private static final int CONTEXT_MENU_EDIT = Menu.FIRST;
    private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 1;
    
    private static final int OPTIONS_MENU_ADD = Menu.FIRST;
    private static final int OPTIONS_MENU_IMPORT = 2;
    private static final int OPTIONS_MENU_EXPORT = 3;

    private static final int DIALOG_ADD_OR_EDIT = 0;
    private static final int DIALOG_IMPORT = 1;
    private static final int DIALOG_EXPORT = 2;
    private static final int DIALOG_ERROR = 3;
    
    /** The word being edited in the dialog (null means the user is adding a word). */
    private String mDialogEditingWord;
    
    private Cursor mCursor;
    
    private boolean mAddedWordAlready;
    private boolean mAutoReturn;
    
    private String mImportExportError;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_content_with_empty_view);
        
        mCursor = createCursor();
        setListAdapter(createAdapter());
        
        TextView emptyView = (TextView) findViewById(R.id.empty);
        emptyView.setText(R.string.user_dict_settings_empty_text);
        
        ListView listView = getListView();
        listView.setFastScrollEnabled(true);
        listView.setEmptyView(emptyView);

        registerForContextMenu(listView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!mAddedWordAlready 
                && getIntent().getAction().equals("com.android.inputmethod.norwegian.USER_DICTIONARY_INSERT")) {
            String word = getIntent().getStringExtra(EXTRA_WORD);
            mAutoReturn = true;
            if (word != null) {
                showAddOrEditDialog(word);
            }
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mDialogEditingWord = state.getString(INSTANCE_KEY_DIALOG_EDITING_WORD);
        mAddedWordAlready = state.getBoolean(INSTANCE_KEY_ADDED_WORD, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_KEY_DIALOG_EDITING_WORD, mDialogEditingWord);
        outState.putBoolean(INSTANCE_KEY_ADDED_WORD, mAddedWordAlready);
    }

    private Cursor createCursor() {
        String currentLocale = Locale.getDefault().toString();
        // Case-insensitive sort
        return managedQuery(UserDictionary.Words.CONTENT_URI, QUERY_PROJECTION,
                QUERY_SELECTION, new String[] { currentLocale },
                "UPPER(" + UserDictionary.Words.WORD + ")");
    }

    private ListAdapter createAdapter() {
        return new MyAdapter(this,
                android.R.layout.simple_list_item_1, mCursor,
                new String[] { UserDictionary.Words.WORD },
                new int[] { android.R.id.text1 });
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        openContextMenu(v);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (!(menuInfo instanceof AdapterContextMenuInfo)) return;
        
        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(getWord(adapterMenuInfo.position));
        menu.add(0, CONTEXT_MENU_EDIT, 0, 
                R.string.user_dict_settings_context_menu_edit_title);
        menu.add(0, CONTEXT_MENU_DELETE, 0, 
                R.string.user_dict_settings_context_menu_delete_title);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo menuInfo = item.getMenuInfo();
        if (!(menuInfo instanceof AdapterContextMenuInfo)) return false;
        
        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        String word = getWord(adapterMenuInfo.position);
        
        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                deleteWord(word);
                return true;
                
            case CONTEXT_MENU_EDIT:
                showAddOrEditDialog(word);
                return true;
        }
        
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, OPTIONS_MENU_ADD, 0, R.string.user_dict_settings_add_menu_title)
                .setIcon(R.drawable.ic_menu_add);
        menu.add(0, OPTIONS_MENU_IMPORT, 1, R.string.import_title)
                .setIcon(R.drawable.ic_menu_import);
        menu.add(0, OPTIONS_MENU_EXPORT, 2, R.string.export_title)
                .setIcon(R.drawable.ic_menu_export);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case OPTIONS_MENU_ADD:
            showAddOrEditDialog(null);
            break;
        case OPTIONS_MENU_IMPORT:
            showDialog(DIALOG_IMPORT);
            break;
        case OPTIONS_MENU_EXPORT:
            showDialog(DIALOG_EXPORT);
            break;
        }
        return true;
    }

    private void showAddOrEditDialog(String editingWord) {
        mDialogEditingWord = editingWord;
        showDialog(DIALOG_ADD_OR_EDIT);
    }
    
    private String getWord(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(
                mCursor.getColumnIndexOrThrow(UserDictionary.Words.WORD));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
            case DIALOG_ADD_OR_EDIT:
            View content = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
            final EditText editText = (EditText) content.findViewById(R.id.edittext);
            // No prediction in soft keyboard mode. TODO: Create a better way to disable prediction
            editText.setInputType(InputType.TYPE_CLASS_TEXT 
                    | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            
            dialog =  new AlertDialog.Builder(this)
                    .setTitle(mDialogEditingWord != null 
                            ? R.string.user_dict_settings_edit_dialog_title 
                            : R.string.user_dict_settings_add_dialog_title)
                    .setView(content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onAddOrEditFinished(editText.getText().toString());
                            if (mAutoReturn) finish();
                        }})
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAutoReturn) finish();
                        }})
                    .create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            
            break;
            
            case DIALOG_IMPORT:
            case DIALOG_EXPORT:
                View importContent = getLayoutInflater().inflate(R.layout.dialog_import_export, null);
                final EditText pathEditText = (EditText) importContent.findViewById(R.id.import_export_path);
                pathEditText.setInputType(InputType.TYPE_CLASS_TEXT 
                        | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                if(!path.endsWith(File.separator))
                    path += File.separator;
                pathEditText.setText(path);
                final CheckBox keepExisting = (CheckBox) importContent.findViewById(R.id.import_keep_existing);
                
                Builder builder =  new AlertDialog.Builder(this)
                        .setView(importContent)
                        .setNegativeButton(android.R.string.cancel, null);
                
                if(id == DIALOG_IMPORT) {
                    builder.setTitle(R.string.import_title)
                    .setPositiveButton(R.string.import_title, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { 
                            importWords(pathEditText.getText().toString(), keepExisting.isChecked());
                        }});
                } else {
                    keepExisting.setVisibility(View.GONE);
                    builder.setTitle(R.string.export_title)
                        .setPositiveButton(R.string.export_title, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { 
                                    exportWords(pathEditText.getText().toString());
                            }});
                }
                
                dialog = builder.create();
                break;
                
            case DIALOG_ERROR:
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setNegativeButton(android.R.string.ok, null)
                        .setMessage(mImportExportError)
                        .create();
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
        if(id == DIALOG_ADD_OR_EDIT) {
            AlertDialog dialog = (AlertDialog) d;
            d.setTitle(mDialogEditingWord != null 
                            ? R.string.user_dict_settings_edit_dialog_title 
                            : R.string.user_dict_settings_add_dialog_title);
            EditText editText = (EditText) dialog.findViewById(R.id.edittext);
            editText.setText(mDialogEditingWord);
        }
    }

    private void onAddOrEditFinished(String word) {
        onAddOrEditFinished(word, 250);
    }
    
    protected void onAddOrEditFinished(String word, int frequency) {
        if (mDialogEditingWord != null) {
            // The user was editing a word, so do a delete/add
            deleteWord(mDialogEditingWord);
        }
        
        // Disallow duplicates
        deleteWord(word);
        
        // TODO: present UI for picking whether to add word to all locales, or current.
        UserDictionary.Words.addWord(this, word.toString(),
                frequency, UserDictionary.Words.LOCALE_TYPE_ALL);
        mCursor.requery();
        mAddedWordAlready = true;
    }

    protected void deleteWord(String word) {
        getContentResolver().delete(UserDictionary.Words.CONTENT_URI, DELETE_SELECTION,
                new String[] { word });
    }
    
    private void importWords(String path, boolean keepExisting) {
        UserDictionaryBackup ub = new UserDictionaryBackup(this, mCursor);
        int returnCode = ub.importWords(path, keepExisting);
        if(returnCode != 0) {
            switch (returnCode) {
                case 1:
                    mImportExportError = getResources().getString(R.string.import_error_1);
                    break;
                case 2:
                    mImportExportError = getResources().getString(R.string.import_error_2);
                    break;
                case 3:
                    mImportExportError = getResources().getString(R.string.import_error_3);
                    break;
            }
            showDialog(DIALOG_ERROR);
        }
    }
    
    private void exportWords(String path) {
        UserDictionaryBackup ub = new UserDictionaryBackup(this, mCursor);
        int returnCode = ub.exportWords(path);
        if(returnCode == 1) {
            mImportExportError = getResources().getString(R.string.export_error_1);
            showDialog(DIALOG_ERROR);
        }
    }
    
    private static class MyAdapter extends SimpleCursorAdapter implements SectionIndexer {
        private AlphabetIndexer mIndexer;
        
        public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);

            int wordColIndex = c.getColumnIndexOrThrow(UserDictionary.Words.WORD);
            String alphabet = context.getString(R.string.fast_scroll_alphabet);
            mIndexer = new AlphabetIndexer(c, wordColIndex, alphabet); 
        }

        public int getPositionForSection(int section) {
            return mIndexer.getPositionForSection(section);
        }

        public int getSectionForPosition(int position) {
            return mIndexer.getSectionForPosition(position);
        }

        public Object[] getSections() {
            return mIndexer.getSections();
        }
    }
}

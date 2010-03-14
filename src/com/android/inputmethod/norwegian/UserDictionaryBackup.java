package com.android.inputmethod.norwegian;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.database.Cursor;

public class UserDictionaryBackup {

    private static final int INDEX_WORD = 1;
    private static final int INDEX_FREQUENCY = 2;

	private UserDictionarySettings userDictionarySettings;
	private Cursor mCursor;
	private ArrayList<Word> words;
	
	public UserDictionaryBackup(UserDictionarySettings uds, Cursor cursor) {
		this.userDictionarySettings = uds;
		this.mCursor = cursor;
		this.words = new ArrayList<Word>();
	}
	
    private void getWords() {
        final int maxWordLength = ExpandableDictionary.MAX_WORD_LENGTH;
        if (mCursor.moveToFirst()) {
            while (!mCursor.isAfterLast()) {
                String word = mCursor.getString(INDEX_WORD);
                int frequency = mCursor.getInt(INDEX_FREQUENCY);
                // Safeguard against adding really long words. Stack may overflow due
                // to recursion
                if (word.length() < maxWordLength) {
                    words.add(new Word(word, frequency));
                }
                mCursor.moveToNext();
            }
        }
    }
    
    public int importWords(String path, boolean keepExisting) {
    	ArrayList<Word> newWords = new ArrayList<Word>();
    	int returnCode = readFile(path, newWords);
    	if(returnCode == 0) {
    		if(!keepExisting) {
    			getWords();
    			for (int i = 0; i < words.size(); i++)
    				userDictionarySettings.deleteWord(words.get(i).word);
    		}
    		for (int i = 0; i < newWords.size(); i++)
    			userDictionarySettings.onAddOrEditFinished(newWords.get(i).word, newWords.get(i).frequency);
    	}
    	return returnCode;
    }
    
    public int exportWords(String path) {
    	getWords();
    	String wordsString = "";
    	for (int i = 0; i < words.size(); i++)
			wordsString += words.get(i) + "\n";
    	return writeFile(path, wordsString);
    }
    
    private int readFile(String path, ArrayList<Word> returnWords) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
	    	String line;
	    	while((line = bufferedReader.readLine()) != null) {
	    		if(!"".equals(line)) {
	    			String[] l = line.split(" ");
	    			int freq;
	    			if(l.length == 1 || (freq = Integer.parseInt(l[1])) == 0)
	    				freq = 250;
	    			returnWords.add(new Word(l[0], freq));
	    		}
	    	}
	    	bufferedReader.close();
		} catch (FileNotFoundException e) {
			return 1;
		} catch (IOException e) {
			return 2;
		} catch (NumberFormatException e) {
			return 3;
		}
    	return 0;
    }
    
    private int writeFile(String path, String words) {
    	try {
    		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path, false));
    		bufferedWriter.write(words);
    		bufferedWriter.close();
		} catch (IOException e) {
			return 1;
		}
    	return 0;
    }
}

class Word {
	public String word;
	public int frequency;
	
	public Word(String word, int frequency) {
		this.word = word;
		this.frequency = frequency;
	}
	
	public String toString() {
		return word + " " + frequency;
	}
}
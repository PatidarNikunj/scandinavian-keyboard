package com.android.inputmethod.norwegian;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

public class FindDictionary {

	private String defaultPkgPrefix = "com.keyboard.scandinavian.dictionary.";
	private String secondDefaultPkgPrefix = "com.android.inputmethod.norwegian.";
	private String secondDefaultPkgSuffix = "dictionary";
	private String latinPkg;
	private String latinVoicePkg;
	private int SDKVersion;
	
	private Context context;
	private String latinDictInstalled;
	private String[] dictPkgNames;
	
	public FindDictionary(Context context) {
		this.context = context;
		this.latinPkg = context.getResources().getString(R.string.dictionary_builtin_pkg);
		this.latinVoicePkg = context.getResources().getString(R.string.dictionary_builtin_voice_pkg);
		this.SDKVersion = Integer.parseInt(Build.VERSION.SDK);
		
    	ArrayList<String> dictPackagesTmp = new ArrayList<String>();
    	List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
    	for (int i = 0; i < installedPackages.size(); i++) {
    		String pkg = installedPackages.get(i).packageName;
    		if(pkg.startsWith(defaultPkgPrefix) || pkg.startsWith(secondDefaultPkgPrefix) && pkg.endsWith(secondDefaultPkgSuffix))
    			dictPackagesTmp.add(pkg);
    		else if (SDKVersion < 7 && pkg.equals(latinPkg) || SDKVersion == 7 && pkg.equals(latinVoicePkg)) {
    			dictPackagesTmp.add(pkg);
    			latinDictInstalled = pkg;
    		}
    	}
    	this.dictPkgNames = dictPackagesTmp.toArray(new String[dictPackagesTmp.size()]);
	}
	
	public String[] getAppNames() {
		String[] appNames = new String[dictPkgNames.length];
		for (int i = 0; i < dictPkgNames.length; i++) {
			String language = dictPkgNames[i].replace(defaultPkgPrefix, "").replace(secondDefaultPkgPrefix, "").replace(secondDefaultPkgSuffix, "");
			if(language.equals(latinPkg) || language.equals(latinVoicePkg))
				language = context.getResources().getString(R.string.dictionary_builtin_language);
			appNames[i] = (language.substring(0, 1).toUpperCase() + language.substring(1));
		}
		return appNames;
	}
	
	public String[] getPackageNames() {
		return dictPkgNames;
	}
	
	public String findPackageName(String language) {
	    for(String pkg : dictPkgNames)
			if(pkg.endsWith(language.toLowerCase()) || pkg.endsWith(language.toLowerCase() + secondDefaultPkgSuffix))
				return pkg;
		if(latinDictInstalled != null)
            return latinDictInstalled;
		return context.getPackageName();
	}
	
	public int getResId(String pkgName) {
		if(pkgName.startsWith(secondDefaultPkgPrefix))
			return 0x7f030000;
		else if(pkgName.startsWith(defaultPkgPrefix))
			return 0x7f040000;
		else if(pkgName.equals(latinPkg))
			return 0x7f050000;
		else if(pkgName.equals(latinVoicePkg))
			return 0x7f050007;
		else
			return 0;
	}
}

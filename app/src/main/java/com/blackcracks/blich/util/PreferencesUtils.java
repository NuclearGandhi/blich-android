/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class PreferencesUtils {
    //Preferences
    public static String getString(Context context,
                                   String key,
                                   String defaultValue,
                                   boolean isUri) {
        String returnString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
        if (isUri) return returnString;
        else return returnString.replace("/", "");
    }

    public static String getString(Context context,
                                   int prefKey) {
        String key = Constants.Preferences.getKey(context, prefKey);
        String defaultValue = (String) Constants.Preferences.getDefault(context, prefKey);

        return getString(context, key, defaultValue, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    public static boolean getBoolean(Context context, int prefKey) {
        String key = Constants.Preferences.getKey(context, prefKey);
        boolean defaultValue = (boolean) Constants.Preferences.getDefault(context, prefKey);

        return getBoolean(context, key, defaultValue);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, defaultValue);
    }
}
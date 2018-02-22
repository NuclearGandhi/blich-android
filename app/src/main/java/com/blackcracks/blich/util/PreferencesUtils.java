/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.blackcracks.blich.util.Constants.Preferences;

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

    @SuppressWarnings("SameParameterValue")
    public static String getString(Context context,
                                   int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        String defaultValue = (String) Preferences.getDefault(context, prefKey);

        return getString(context, key, defaultValue, false);
    }

    private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    public static boolean getBoolean(Context context, int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        boolean defaultValue = (boolean) Preferences.getDefault(context, prefKey);

        return getBoolean(context, key, defaultValue);
    }

    private static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, defaultValue);
    }

    public static int getInt(Context context, int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        int defaultValue = (int) Preferences.getDefault(context, prefKey);

        return getInt(context, key, defaultValue);
    }
}

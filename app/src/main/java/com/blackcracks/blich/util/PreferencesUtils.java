/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.blackcracks.blich.util.Constants.Preferences;

/**
 * Utilities methods to easily obtain preference values.
 */
public class PreferencesUtils {

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

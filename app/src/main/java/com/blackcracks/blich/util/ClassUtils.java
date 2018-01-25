/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class ClassUtils {

    public static String getCurrentClass(Context context) {

        @Constants.Preferences.PrefIntKeys int intKey = Constants.Preferences.PREF_CLASS_PICKER_KEY;
        String key = Constants.Preferences.getKey(context, intKey);

        String defaultValue = (String) Constants.Preferences.getDefault(context, intKey);

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        key,
                        defaultValue.replace("/", ""));
    }
}

/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.blackcracks.blich.R;

public class NewsUtils {

    public static void setIsFetchingForCategory(Context context, int category, boolean isFetching) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                context.getResources().getString(R.string.pref_is_fetching_news_key) + category,
                isFetching
        ).apply();
    }

    public static boolean getIsFetchingForCategory(Context context, int category) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getResources().getString(R.string.pref_is_fetching_news_key) + category,
                false
        );
    }

    public static void resetIsFetchingPreferences(Context context) {
        for (int i = 0; i <= 4; i++) {
            setIsFetchingForCategory(context, i, false);
        }
    }

    public static void setPreferenceLatestUpdateForCategory(Context context,
                                                            int category,
                                                            long epoch) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(
                context.getResources().getString(R.string.pref_latest_update_key) + category,
                epoch
        ).apply();
    }

    public static long getLatestUpdateForCategory(Context context, int category) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(
                context.getResources().getString(R.string.pref_latest_update_key) + category,
                0
        );
    }

    public static String getActionForCategory(int category) {
        return Constants.IntentConstants.ACTION_FETCH_NEWS_CALLBACK + category;
    }
}

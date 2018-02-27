/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.blackcracks.blich.R;
import com.blackcracks.blich.dialog.ClassPickerDialog;
import com.blackcracks.blich.widget.BlichWidgetProvider;

import java.util.Locale;

/**
 * A class containing general utility methods.
 */
public class Utilities {

    /**
     * Check for network connectivity.
     *
     * @return {@code true} there is network connection.
     */
    public static boolean isThereNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //noinspection ConstantConditions
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Check if this is the first time the user launched the app.
     *
     * @return {@code true} this is first launch.
     */
    public static boolean isFirstLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ClassPickerDialog.PREF_IS_FIRST_LAUNCH_KEY, true);
    }

    /**
     * Change {@link Locale} to Hebrew, right to left.
     */
    public static void setLocaleToHebrew(Context context) {
        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getApplicationContext().createConfigurationContext(config);

    }

    /**
     * Update the widget on the home screen.
     */
    public static void updateWidget(Context context) {
        ComponentName widget = new ComponentName(context, BlichWidgetProvider.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(widget, views);
    }

}

/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.ClassGroup;
import com.blackcracks.blich.widget.BlichWidgetProvider;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.realm.Realm;

/**
 * A class containing general utility methods.
 */
public class Utilities {

    private static final String PROPERTY_CLASS_GROUP_ID = "class_group_id";
    private static final String PROPERTY_CLASS_GROUP_GRADE = "class_group_grade";

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
     * Get the current theme
     *
     * @return A key representing a theme
     */
    public static String getATEKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    /**
     * Get the user's class group and save its values to Crashlytics and Firebase.
     *
     */
    public static void setClassGroupProperties(Context context) {
        Realm realm = Realm.getDefaultInstance();
        int id = PreferenceUtils.getInstance().getInt(R.string.pref_user_class_group_key);
        ClassGroup classGroup = RealmUtils.getGrade(realm, id);
        setClassGroupProperties(context, classGroup.getId(), classGroup.getGrade());
    }

    /**
     * Save the class group's values to Crashlytics and Firebase.
     *
     * @param id    the class group's id
     * @param grade the class group's grade
     */
    public static void setClassGroupProperties(Context context, int id, int grade) {
        FirebaseAnalytics.getInstance(context).setUserProperty(PROPERTY_CLASS_GROUP_ID, "" + id);
        FirebaseAnalytics.getInstance(context).setUserProperty(PROPERTY_CLASS_GROUP_GRADE, "" + grade);

        Crashlytics.setInt(PROPERTY_CLASS_GROUP_ID, id);
        Crashlytics.setInt(PROPERTY_CLASS_GROUP_GRADE, grade);
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

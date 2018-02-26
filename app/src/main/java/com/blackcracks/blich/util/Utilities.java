/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.BaseThemedActivity;
import com.blackcracks.blich.dialog.ClassPickerDialog;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.widget.BlichWidgetProvider;

import java.util.Locale;

import timber.log.Timber;

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

    public static void initializeSync(Context context) {
        BlichSyncUtils.initializeJobService(context);
        syncDatabase(context);
    }

    /**
     * Begin sync.
     */
    public static void syncDatabase(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        boolean isConnected = false;
        boolean isFetching = PreferencesUtils.getBoolean(
                context,
                Preferences.PREF_IS_SYNCING_KEY
        );
        if (!isFetching) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(context.getString(R.string.pref_is_syncing_key), true)
                    .apply();
            isConnected = Utilities.isThereNetworkConnection(context);
            if (isConnected) {
                BlichSyncUtils.startImmediateSync(context);
            } else {
                onSyncFinished(context, BlichSyncTask.FETCH_STATUS_NO_CONNECTION, null);
            }
        }
        if (BuildConfig.DEBUG) {
            Timber.d("syncDatabase() called" +
                    ", isFetching = " + isFetching +
                    ", isConnected = " + isConnected);
        }
    }

    /**
     * Callback for when sync finished.
     *
     * @param status a {@link com.blackcracks.blich.sync.BlichSyncTask.FetchStatus} returned from
     *               the sync.
     * @param fragmentManager a {@link FragmentManager} for displaying dialogs.
     */
    @SuppressLint("SwitchIntDef")
    public static void onSyncFinished(final Context context,
                                      @BlichSyncTask.FetchStatus int status,
                                      @Nullable FragmentManager fragmentManager) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        if (status == BlichSyncTask.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(((BaseThemedActivity) context).getRootView(),
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();

        } else if (status == BlichSyncTask.FETCH_STATUS_CLASS_NOT_CONFIGURED) {

            if (fragmentManager != null) {
                ClassPickerDialog dialogFragment = new ClassPickerDialog();
                dialogFragment.show(fragmentManager, "choose_class");
                dialogFragment.setOnDestroyListener(new ClassPickerDialog.OnDestroyListener() {
                    @Override
                    public void onDestroy(Context context) {
                        //Start the periodic syncing of
                        BlichSyncUtils.initializeJobService(context);
                        Utilities.syncDatabase(context);
                    }
                });
            } else {
                throw new NullPointerException("A non-null fragment manager is required " +
                        "in case the user's class isn't configured");
            }

        } else {
            @SuppressLint("InflateParams")
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncTask.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncTask.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = dialogView.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = dialogView.findViewById(R.id.dialog_message);
            message.setText(messageString);

            new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_try_again,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    syncDatabase(context);
                                }
                            })
                    .setNegativeButton(R.string.dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
        }
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

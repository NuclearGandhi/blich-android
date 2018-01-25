package com.blackcracks.blich.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.widget.BlichWidgetProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class Utilities {

    public static boolean isThereNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isFirstLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ChooseClassDialogFragment.PREF_IS_FIRST_LAUNCH_KEY, true);
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static long getTimeInMillisFromDate(String date) {

        Locale locale = new Locale("iw");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        Date examDate = null;
        try {
            examDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Timber.d(e, e.getMessage());
        }

        return examDate.getTime();
    }

    public static void initializeBlichDataUpdater(Context context, View view) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        updateBlichData(context, view);
    }

    //Call BlichSyncTask to begin a sync
    public static void updateBlichData(Context context, View view) {

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
                onSyncFinished(context, view, BlichSyncTask.FETCH_STATUS_NO_CONNECTION, null);
            }
        }
        if (BuildConfig.DEBUG) {
            Timber.d("updateBlichData() called" +
                    ", isFetching = " + isFetching +
                    ", isConnected = " + isConnected);
        }
    }

    //Callback from BlichSyncTask's sync
    @SuppressLint("SwitchIntDef")
    public static void onSyncFinished(final Context context,
                                      final View view,
                                      @BlichSyncTask.FetchStatus int status,
                                      @Nullable FragmentManager fragmentManager) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        if (status == BlichSyncTask.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(view,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();

        } else if (status == BlichSyncTask.FETCH_STATUS_CLASS_NOT_CONFIGURED) {

            if (fragmentManager != null) {
                ChooseClassDialogFragment dialogFragment = new ChooseClassDialogFragment();
                dialogFragment.show(fragmentManager, "choose_class");
                dialogFragment.setOnDestroyListener(new ChooseClassDialogFragment.OnDestroyListener() {
                    @Override
                    public void onDestroy(Context context) {
                        //Start the periodic syncing of
                        BlichSyncUtils.initialize(context);
                        Utilities.initializeBlichDataUpdater(context, view);
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
                                    updateBlichData(context, view);
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

    public static void setLocaleToHebrew(Context context) {
        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getApplicationContext().createConfigurationContext(config);

    }


    public static void updateWidget(Context context) {
        ComponentName widget = new ComponentName(context, BlichWidgetProvider.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(widget, views);
    }

}

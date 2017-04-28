package com.blackcracks.blich.util;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();


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

    public static String getPreferenceString(Context context,
                                             String key,
                                             String defaultValue,
                                             boolean isUri) {
        String returnString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
        if (isUri) return returnString;
        else return returnString.replace("/", "");
    }

    public static boolean getPreferenceBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    public static int getPreferenceInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
        .getInt(key, defaultValue);
    }

    public static long getPreferenceLong(Context context, String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(key, defaultValue);
    }

    public static long getTimeInMillisFromDate(String date) {

        Locale locale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        Date examDate = null;
        try {
            examDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return examDate.getTime();
    }

    public static void initializeBlichDataUpdater(Context context, View view) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_fetching_key), false)
                .apply();

        updateBlichData(context, view);
    }

    //Call BlichSyncAdapter to begin a sync
    public static void updateBlichData(Context context, View view) {

        boolean isConnected = false;
        boolean isFetching = getPreferenceBoolean(context, context.getString(R.string.pref_is_fetching_key), false);
        if (!isFetching) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(context.getString(R.string.pref_is_fetching_key), true)
                    .apply();
            isConnected = Utilities.isThereNetworkConnection(context);
            if (isConnected) {
                BlichSyncAdapter.syncImmediately(context);
            } else {
                onSyncFinished(context, view, BlichSyncAdapter.FETCH_STATUS_NO_CONNECTION);
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "updateBlichData() called" +
                    ", isFetching = " + isFetching +
                    ", isConnected = " + isConnected);
        }
    }

    //Callback from BlichSyncAdapter's sync
    public static void onSyncFinished(final Context context, final View view, @BlichSyncAdapter.FetchStatus int status) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_fetching_key), false)
                .apply();

        if (status == BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(view,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncAdapter.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncAdapter.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = (TextView) dialogView.findViewById(R.id.dialog_message);
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

}

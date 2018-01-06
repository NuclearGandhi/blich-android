package com.blackcracks.blich.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.Constants.Preferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmConfiguration;
import timber.log.Timber;

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


    //Preferences
    public static String getPrefString(Context context,
                                       String key,
                                       String defaultValue,
                                       boolean isUri) {
        String returnString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
        if (isUri) return returnString;
        else return returnString.replace("/", "");
    }

    public static String getPrefString(Context context,
                                       int prefKey) {
        String key = Preferences.getKey(context, prefKey);
        String defaultValue = (String) Preferences.getDefault(context, prefKey);

        return getPrefString(context, key, defaultValue, false);
    }

    public static boolean getPrefBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    public static boolean getPrefBoolean(Context context, int prefKey) {
            String key = Preferences.getKey(context, prefKey);
            boolean defaultValue = (boolean) Preferences.getDefault(context, prefKey);

            return getPrefBoolean(context, key, defaultValue);
    }

    public static int getPrefInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, defaultValue);
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
        boolean isFetching = getPrefBoolean(
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

    public static class Realm {
        public static void setUpRealm(Context context) {
            io.realm.Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder().build();
            io.realm.Realm.setDefaultConfiguration(config);
        }
    }

    public static class News {

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

    public static class Schedule {

        public static int getWantedDayOfTheWeek() {

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hour >= 18 && day != 7)
                day++; //Move to the next day if it is later than 18:00, unless it is Saturday.
            if (day == 7) day = 1; //If it is Saturday, set day to 1 (Sunday).
            return day;
        }
    }

    public static class Class {

        public static String getCurrentClass(Context context) {

            @Preferences.PrefIntKeys int intKey = Preferences.PREF_CLASS_PICKER_KEY;
            String key = Preferences.getKey(context, intKey);

            String defaultValue = (String) Preferences.getDefault(context, intKey);

            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(
                            key,
                            defaultValue.replace("/", ""));
        }
    }
}

package com.blackcracks.blich.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.SettingsFragment;

import java.util.HashMap;
import java.util.List;

public class Utilities {

    private static final HashMap<String, Object> PREFERENCES = new HashMap<>();

    private static final String LOG_TAG = Utilities.class.getSimpleName();

    public static void initializeUtils() {

        PREFERENCES.put(SettingsFragment.PREF_CLASS_PICKER_KEY, "ט'3");
        PREFERENCES.put(SettingsFragment.PREF_NOTIFICATION_TOGGLE_KEY, true);
        PREFERENCES.put(SettingsFragment.PREF_NOTIFICATION_SOUND_KEY,
                Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }


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

    public static String getPreferenceString(Context context, String key) {

        Log.d(LOG_TAG, key);

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key,
                        (String) PREFERENCES.get(key))
                .replace("/", "");
    }

    public static String getPreferenceString(Context context, String key, boolean isUri) {
        String returnString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key,
                        (String) PREFERENCES.get(key));
        if (isUri) return returnString;
        else return returnString.replace("/", "");
    }

    public static boolean getPreferenceBoolean(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key,
                        (boolean) PREFERENCES.get(key));
    }
}

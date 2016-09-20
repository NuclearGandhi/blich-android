package com.blackcracks.blich.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.SettingsFragment;

import java.util.HashMap;
import java.util.List;

public class Utilities {

    public static final HashMap<String, Object> PREFERENCES = new HashMap<>();

    public static void initializeUtils() {

        PREFERENCES.put(SettingsFragment.PREF_CLASS_PICKER_KEY, "ט'3");
        PREFERENCES.put(SettingsFragment.PREF_NOTIFICATION_TOGGLE_KEY, true);
    }


    public static boolean isThereNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
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
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key,
                        (String) PREFERENCES.get(key))
                .replace("/", "");
    }

    public static boolean getPreferenceBoolean(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key,
                        (boolean) PREFERENCES.get(key));
    }
}

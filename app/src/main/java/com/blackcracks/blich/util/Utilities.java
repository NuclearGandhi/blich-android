package com.blackcracks.blich.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.SettingsFragment;

import java.util.HashMap;

public class Utilities {

    public static final HashMap<String, Object> PREFERENCES = new HashMap<>();

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

    public static void initializeUtils() {

        PREFERENCES.put(SettingsFragment.PREF_CLASS_PICKER_KEY, "ט'3");
        PREFERENCES.put(SettingsFragment.PREF_NOTIFICATION_TOGGLE_KEY, true);
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

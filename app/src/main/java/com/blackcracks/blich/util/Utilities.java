package com.blackcracks.blich.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.blackcracks.blich.fragment.ChooseClassDialogFragment;

public class Utilities {

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
}

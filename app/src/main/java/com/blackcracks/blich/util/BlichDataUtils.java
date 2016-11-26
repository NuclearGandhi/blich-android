package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.SettingsActivity;

public class BlichDataUtils {

    public static class ClassUtils {

        public static String getCurrentClass(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(SettingsActivity.SettingsFragment.PREF_CLASS_PICKER_KEY,
                            context.getResources().getString(R.string.pref_class_picker_default_value)
                                    .replace("/", ""));
        }
    }
}

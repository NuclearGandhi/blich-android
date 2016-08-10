package com.blackcracks.blich.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.SettingsFragment;

public class BlichDataUtils {

    public static class ClassUtils {

        private static int[] sMaxGradeNumber;
        private static boolean sClassChanged = true;

        public static void setMaxGradeNumber(@NonNull int[] maxGradeNumber) {
            sMaxGradeNumber = maxGradeNumber;
        }

        public static int[] getMaxGradeNumber() {
            return sMaxGradeNumber;
        }

        public static String getCurrentClass(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(SettingsFragment.PREF_CLASS_PICKER_KEY,
                            context.getResources().getString(R.string.pref_class_picker_default_value))
                    .replace("/", "");
        }

        public static boolean isClassChanged() {
            return sClassChanged;
        }

        public static void setClassChanged(boolean sClassChanged) {
            ClassUtils.sClassChanged = sClassChanged;
        }
    }
}

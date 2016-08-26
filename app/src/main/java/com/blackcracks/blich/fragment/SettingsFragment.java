package com.blackcracks.blich.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.blackcracks.blich.R;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment;
import com.blackcracks.blich.util.BlichDataUtils;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_CLASS_PICKER_KEY = "class_picker";
    public static final String PREF_IS_SYNCING_ON = "sync_on";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        ClassPickerPreference classPickerPicker =
                (ClassPickerPreference) findPreference(PREF_CLASS_PICKER_KEY);
        String grade = getPreferenceScreen().getSharedPreferences().getString(PREF_CLASS_PICKER_KEY,
                getResources().getString(R.string.pref_class_picker_default_value))
                .replace("/", "");

        classPickerPicker.setSummary(grade);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        //noinspection ConstantConditions
        activity.getSupportActionBar().setTitle(R.string.drawer_settings_title);
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        PreferenceDialogFragmentCompat fragment = null;
        if (preference instanceof ClassPickerPreference) {
            fragment = ClassPickerPreferenceDialogFragment.newInstance(preference);
        }
        if (fragment != null) {
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "fragment");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_CLASS_PICKER_KEY: {
                ClassPickerPreference classPickerPicker = (ClassPickerPreference) findPreference(key);
                String grade = sharedPreferences.getString(key,
                        getResources().getString(R.string.pref_class_picker_default_value))
                        .replace("/", "");

                classPickerPicker.setSummary(grade);
                BlichDataUtils.ClassUtils.setClassChanged(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
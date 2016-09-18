package com.blackcracks.blich.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.blackcracks.blich.R;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_CLASS_PICKER_KEY = "class_picker";
    public static final String PREF_NOTIFICATION_TOGGLE_KEY = "notification_toggle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        //noinspection ConstantConditions
        activity.getSupportActionBar().setTitle(R.string.drawer_settings_title);

        ClassPickerPreference classPickerPreference =
                (ClassPickerPreference) findPreference(PREF_CLASS_PICKER_KEY);
        String grade = classPickerPreference.getValue();

        classPickerPreference.setSummary(grade);
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
                ClassPickerPreference preference = (ClassPickerPreference) findPreference(key);
                String grade = sharedPreferences.getString(key,
                        getString(R.string.pref_class_picker_default_value));

                preference.setSummary(grade);
            }
            case PREF_NOTIFICATION_TOGGLE_KEY: {
                BlichSyncAdapter.initializeSyncAdapter(getContext());
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
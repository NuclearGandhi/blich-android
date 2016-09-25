package com.blackcracks.blich.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import com.blackcracks.blich.R;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_CLASS_PICKER_KEY = "class_picker";
    public static final String PREF_NOTIFICATION_TOGGLE_KEY = "notification_toggle";
    public static final String PREF_NOTIFICATION_SOUND_KEY = "notification_sound";

    private static final int RINGTONE_PICKER_REQUEST = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        initPrefSummery();
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.drawer_settings_title);
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
    public boolean onPreferenceTreeClick(Preference preference) {

        String key = preference.getKey();

        if (key.equals(PREF_NOTIFICATION_SOUND_KEY)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = Utilities.getPreferenceString(getContext(), key, true);
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_PICKER_REQUEST && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(PREF_NOTIFICATION_SOUND_KEY, uri.toString())
                        .apply();

                Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                Preference preference = findPreference(PREF_NOTIFICATION_SOUND_KEY);
                preference.setSummary(ringtone.getTitle(getContext()));
            } else {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(PREF_NOTIFICATION_SOUND_KEY, "")
                        .apply();
                Preference preference = findPreference(PREF_NOTIFICATION_SOUND_KEY);
                preference.setSummary("שקט");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    private void initPrefSummery() {

        //Class Picker Preference
        ClassPickerPreference classPickerPreference =
                (ClassPickerPreference) findPreference(PREF_CLASS_PICKER_KEY);
        String grade = classPickerPreference.getValue();
        classPickerPreference.setSummary(grade);


        //Notification Sound Preference
        String uri = Utilities.getPreferenceString(getContext(), PREF_NOTIFICATION_SOUND_KEY, true);
        if (!uri.equals("")) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putString(PREF_NOTIFICATION_SOUND_KEY, uri)
                    .apply();

            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(uri));
            Preference preference = findPreference(PREF_NOTIFICATION_SOUND_KEY);
            preference.setSummary(ringtone.getTitle(getContext()));
        } else {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                    .putString(PREF_NOTIFICATION_SOUND_KEY, "")
                    .apply();

            Preference preference = findPreference(PREF_NOTIFICATION_SOUND_KEY);
            preference.setSummary("שקט");
        }
    }
}
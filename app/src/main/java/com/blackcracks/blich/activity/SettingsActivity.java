package com.blackcracks.blich.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.Utilities;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_forward_white_24dp);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final int RINGTONE_PICKER_REQUEST = 100;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_main);
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
            initPrefSummery();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setTitle(R.string.drawer_settings_title);
        }

        @Override
        public void onResume() {
            super.onResume();
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onStop() {
            super.onStop();
            PreferenceManager.getDefaultSharedPreferences(getContext())
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
            //Handle the notification sound preference
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY))) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                String existingValue = Utilities.getPrefString(getContext(),
                        key,
                        (String) Preferences.getDefault(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY),
                        true);
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

            //Handle the notification sound preference
            if (requestCode == RINGTONE_PICKER_REQUEST && data != null) {

                String key = Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY);
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {

                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                            .putString(key, uri.toString())
                            .apply();

                    Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                    Preference preference = findPreference(key);
                    preference.setSummary(ringtone.getTitle(getContext()));
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                            .putString(key, "")
                            .apply();
                    Preference preference = findPreference(key);
                    preference.setSummary("שקט");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_CLASS_PICKER_KEY))) {
                ClassPickerPreference preference = (ClassPickerPreference) findPreference(key);
                String grade = sharedPreferences.getString(key,
                        (String) Preferences.getDefault(getContext(), Preferences.PREF_CLASS_PICKER_KEY));

                preference.setSummary(grade);
                getContext().getContentResolver().delete(
                        BlichContract.ScheduleEntry.CONTENT_URI,
                        null,
                        null
                );
                getContext().getContentResolver().delete(
                        BlichContract.LessonEntry.CONTENT_URI,
                        null,
                        null
                );
                Utilities.updateBlichData(getContext(), getView());
            }
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_TOGGLE_KEY))) {
                BlichSyncAdapter.initializeSyncAdapter(getContext());
            }
        }

        private void initPrefSummery() {

            String classPickerKey =
                    Preferences.getKey(getContext(), Preferences.PREF_CLASS_PICKER_KEY);
            String notificationSoundKey =
                    Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY);

            String notificationSoundDefault =
                    (String) Preferences.getDefault(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY);

            //Class Picker Preference
            ClassPickerPreference classPickerPreference =
                    (ClassPickerPreference) findPreference(classPickerKey);
            String grade = classPickerPreference.getValue();
            classPickerPreference.setSummary(grade);


            //Notification Sound Preference
            String uri = Utilities.getPrefString(getContext(),
                    notificationSoundKey,
                    notificationSoundDefault,
                    true);
            if (!uri.equals("")) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(notificationSoundKey, uri)
                        .apply();

                Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(uri));
                Preference preference = findPreference(notificationSoundKey);
                preference.setSummary(ringtone.getTitle(getContext()));
            } else {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(notificationSoundKey, "")
                        .apply();

                Preference preference = findPreference(notificationSoundKey);
                preference.setSummary("שקט");
            }
        }
    }
}

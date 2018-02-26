/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.supportv7.ATEColorPreference;
import com.afollestad.appthemeengine.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment;
import com.blackcracks.blich.preference.FilterPreference;
import com.blackcracks.blich.preference.FilterPreferenceDialogFragment;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;

import io.realm.Realm;

/**
 * An {@link AppCompatActivity} containing the preference fragment, handling its lifecycle.
 */
public class SettingsActivity extends BaseThemedActivity implements ColorChooserDialog.ColorCallback {

    private static final String FRAGMENT_KEY = "fragment_settings";

    private Fragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Utilities.setLocaleToHebrew(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_forward_white_24dp);
        }

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_KEY);
        } else {
            mFragment = new SettingsFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, mFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save fragment
        getSupportFragmentManager().putFragment(
                outState,
                FRAGMENT_KEY,
                mFragment
        );
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        final Config config = ATE.config(this, getATEKey());
        switch (dialog.getTitle()) {
            case R.string.pref_theme_primary_title:
                config.primaryColor(selectedColor);
                break;
            case R.string.pref_theme_accent_title:
                config.accentColor(selectedColor);
                // We've overridden the navigation view selected colors in the default config,
                // which means we are responsible for keeping those colors up to date.
                config.navigationViewSelectedIcon(selectedColor);
                config.navigationViewSelectedText(selectedColor);
                break;
        }
        config.commit();
        recreate(); // recreation needed to reach the checkboxes in the preferences layout
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    /**
     * {@link Fragment} handling all the the preferences.
     */
    @SuppressWarnings("ConstantConditions")
    public static class SettingsFragment extends ATEPreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener{

        private static final int RINGTONE_PICKER_REQUEST = 100;

        String mAteKey;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_main);
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
            initPrefSummery();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
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
            } else if (preference instanceof FilterPreference) {
                fragment = FilterPreferenceDialogFragment.newInstance(preference);
            }
            if (fragment != null) {
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), "fragment");
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {

            String key = preference.getKey();
            if (key == null) return true;
            //Handle the notification sound preference
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY))) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                String existingValue = PreferencesUtils.getString(getContext(),
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
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_USER_CLASS_GROUP_KEY))) {
                setClassPickerSummery();
                Utilities.updateBlichData(getContext(), getView());
            }
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_TOGGLE_KEY))) {
                BlichSyncUtils.initializeJobService(getContext());
            }
            if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_FILTER_SELECT_KEY))) {
                setFilterSelectSummery();
            }
        }

        private void invalidateSettings() {
            mAteKey = ((SettingsActivity) getActivity()).getATEKey();

            findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Marks both theme configs as changed so MainActivity restarts itself on return
                    Config.markChanged(getActivity(), "light_theme");
                    Config.markChanged(getActivity(), "dark_theme");
                    // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                    // It's used in getATEKey() of both the Activities.
                    getActivity().recreate();
                    return true;
                }
            });

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            int primaryColor = Config.primaryColor(getActivity(), mAteKey);
            int darkPrimaryColor = Config.primaryColorDark(getActivity(), mAteKey);
            primaryColorPref.setColor(primaryColor, darkPrimaryColor);
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder(getActivity(), R.string.pref_theme_primary_title)
                            .preselect(Config.primaryColor(getActivity(), mAteKey))
                            .show(getFragmentManager());
                    return true;
                }
            });

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            int accentColor = Config.accentColor(getActivity(), mAteKey);
            accentColorPref.setColor(accentColor, accentColor);
            accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder(getActivity(), R.string.pref_theme_accent_title)
                            .accentMode(true)
                            .preselect(Config.accentColor(getActivity(), mAteKey))
                            .show(getFragmentManager());
                    return true;
                }
            });
        }

        private void initPrefSummery() {
            setClassPickerSummery();
            setFilterSelectSummery();
            setNotificationSoundPreference();
        }

        //Notification Sound Preference
        private void setNotificationSoundPreference() {
            String notificationSoundKey =
                    Preferences.getKey(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY);

            String notificationSoundDefault =
                    (String) Preferences.getDefault(getContext(), Preferences.PREF_NOTIFICATION_SOUND_KEY);

            String uri = PreferencesUtils.getString(getContext(),
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

        //Class Picker Preference
        private void setClassPickerSummery() {
            String classPickerKey =
                    Preferences.getKey(getContext(), Preferences.PREF_USER_CLASS_GROUP_KEY);
            ClassPickerPreference classPickerPreference =
                    (ClassPickerPreference) findPreference(classPickerKey);

            int classId = classPickerPreference.getValue();
            Realm realm = Realm.getDefaultInstance();
            String grade = RealmUtils.getGrade(realm, classId).getName();
            classPickerPreference.setSummary(grade);
            realm.close();
        }

        //Filter Preference
        private void setFilterSelectSummery() {
            FilterPreference filterPreference =
                    (FilterPreference) findPreference(getString(R.string.pref_filter_select_key));
            if (filterPreference.getValue() != null) {
                String[] teachersAndSubjects = filterPreference.getValue().split(";");
                StringBuilder summary = new StringBuilder();
                for (int i = 0; i < teachersAndSubjects.length; i++) {
                    String[] arr = teachersAndSubjects[i].split(",");
                    summary.append(arr[0]);
                    if (i != teachersAndSubjects.length - 1) summary.append(", ");
                }
                filterPreference.setSummary(summary);
            }
        }
    }
}

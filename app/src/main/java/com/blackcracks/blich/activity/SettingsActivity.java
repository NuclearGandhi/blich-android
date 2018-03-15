/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;

import android.content.Context;
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
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.supportv7.ATEColorPreference;
import com.afollestad.appthemeengine.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.ClassGroup;
import com.blackcracks.blich.dialog.ClassPickerDialog;
import com.blackcracks.blich.preference.ClassPickerPreference;
import com.blackcracks.blich.dialog.FilterDialog;
import com.blackcracks.blich.preference.FilterPreference;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.SyncUtils;
import com.blackcracks.blich.util.Utilities;

import io.realm.Realm;

/**
 * An {@link AppCompatActivity} containing the preference fragment, handling its lifecycle.
 */
public class SettingsActivity extends BaseThemedActivity implements ColorChooserDialog.ColorCallback {

    private static final String FRAGMENT_KEY = "fragment_settings";

    private View mRootView;
    private Fragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootView = LayoutInflater.from(this).inflate(
                R.layout.activity_settings, null, false);
        setContentView(mRootView);

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
            case R.string.pref_theme_lesson_canceled_title: {
                PreferenceUtils.getInstance().putInt(R.string.pref_theme_lesson_canceled_key, selectedColor);
                break;
            }
            case R.string.pref_theme_lesson_changed_title: {
                PreferenceUtils.getInstance().putInt(R.string.pref_theme_lesson_changed_key, selectedColor);
                break;
            }
            case R.string.pref_theme_lesson_event_title: {
                PreferenceUtils.getInstance().putInt(R.string.pref_theme_lesson_event_key, selectedColor);
                break;
            }
            case R.string.pref_theme_lesson_exam_title: {
                PreferenceUtils.getInstance().putInt(R.string.pref_theme_lesson_exam_key, selectedColor);
                break;
            }
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
            implements SharedPreferences.OnSharedPreferenceChangeListener,
            PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

        private static final String SUBSCREEN_KEY = "sub_screen";

        private static final int RINGTONE_PICKER_REQUEST = 100;
        private static final String DIALOG_TAG = "dialog";

        String mAteKey;
        String mCurrentSubscreenKey = "root";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            if (savedInstanceState != null &&
                    savedInstanceState.containsKey(SUBSCREEN_KEY) &&
                    !savedInstanceState.getString(SUBSCREEN_KEY).equals("root")) {
                mCurrentSubscreenKey = rootKey = savedInstanceState.getString(SUBSCREEN_KEY);
            }
            setPreferencesFromResource(R.xml.pref_main, rootKey);
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
            initPrefSummery();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int backgroundColor;
            if (Utilities.getATEKey(getContext()).equals("light_theme")) {
                backgroundColor = ContextCompat.getColor(getContext(), R.color.material_background_light);
            } else {
                backgroundColor = ContextCompat.getColor(getContext(), R.color.material_background_dark);
            }
            view.setBackgroundColor(backgroundColor);
            invalidateSettings();
        }

        @Override
        public void onStart() {
            super.onStart();
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(SUBSCREEN_KEY, mCurrentSubscreenKey);
        }

        @Override
        public void onStop() {
            super.onStop();
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDisplayPreferenceDialog(final Preference preference) {
            PreferenceDialogFragmentCompat fragment = null;

            if (preference instanceof ClassPickerPreference) {
                ClassPickerDialog dialog = new ClassPickerDialog.Builder()
                        .setDismissible(true)
                        .setDisplayNegativeButton(true)
                        .build();

                dialog.setOnPositiveClickListener(new ClassPickerDialog.OnPositiveClickListener() {
                    @Override
                    public void onDestroy(Context context, int id) {
                        ((ClassPickerPreference) preference).setValue(id);
                    }
                });

                dialog.show(getFragmentManager(), DIALOG_TAG);
            } else if (preference instanceof FilterPreference) {
                showFilterDialog();
            }
            if (fragment != null) {
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), "fragment");
            }
        }

        private void showFilterDialog() {
            FilterDialog dialog = new FilterDialog();

            dialog.setOnPositiveClickListener(new FilterDialog.OnPositiveClickListener() {
                @Override
                public void onPositiveClick(String value) {
                    ((SwitchPreferenceCompat) findPreference(getString(R.string.pref_filter_toggle_key)))
                            .setChecked(!value.equals(""));

                    ((FilterPreference) findPreference(getString(R.string.pref_filter_select_key)))
                            .setValue(value);
                }
            });
            dialog.show(getFragmentManager(), DIALOG_TAG);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {

            String key = preference.getKey();
            if (key == null) return true;
            //Handle the notification sound preference
            if (key.equals(getString(R.string.pref_notification_sound_key))) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

                String existingValue = PreferenceUtils.getInstance().getString(R.string.pref_notification_sound_key);
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

                String key = PreferenceUtils.getInstance().getString(R.string.pref_notification_sound_key);
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
            if (!isAdded()) return;
            if (key.equals(getString(R.string.pref_user_class_group_key))) {
                setClassPickerSummery();
                SyncUtils.syncDatabase(getContext());
            }
            if (key.equals(getString(R.string.pref_notification_toggle_key))) {
                BlichSyncUtils.initializeJobService(getContext());
            }
            if (key.equals(getString(R.string.pref_filter_select_key))) {
                setFilterSelectSummery();
            }
            if (key.equals(getString(R.string.pref_filter_toggle_key)) &&
                    ((SwitchPreferenceCompat) findPreference(getString(R.string.pref_filter_toggle_key))).isChecked() &&
                    ((FilterPreference) findPreference(getString(R.string.pref_filter_select_key))).getValue().equals("")) {
                //If the filter is on and the filter teacher list is empty:
                showFilterDialog();
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

            initColorPreference(
                    "primary_color",
                    Config.primaryColor(getContext(), mAteKey),
                    R.string.pref_theme_primary_title,
                    false);

            initColorPreference(
                    "accent_color",
                    Config.accentColor(getContext(), mAteKey),
                    R.string.pref_theme_accent_title,
                    true);


            initColorPreference(
                    getString(R.string.pref_theme_lesson_canceled_key),
                    PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_canceled_key),
                    R.string.pref_theme_lesson_canceled_title,
                    false);

            initColorPreference(
                    getString(R.string.pref_theme_lesson_changed_key),
                    PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_changed_key),
                    R.string.pref_theme_lesson_changed_title,
                    false);

            initColorPreference(
                    getString(R.string.pref_theme_lesson_exam_key),
                    PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_exam_key),
                    R.string.pref_theme_lesson_exam_title,
                    false);

            initColorPreference(
                    getString(R.string.pref_theme_lesson_event_key),
                    PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_event_key),
                    R.string.pref_theme_lesson_event_title,
                    false);
        }

        private void initColorPreference(String key, final int color, @StringRes final int title, final boolean isAccent) {
            ATEColorPreference colorPref = (ATEColorPreference) findPreference(key);
            int darkColor = ATEUtil.darkenColor(color);
            colorPref.setColor(color, darkColor);
            colorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder(getActivity(), title)
                            .accentMode(isAccent)
                            .preselect(color)
                            .show(getFragmentManager());
                    return true;
                }
            });
        }

        private void initPrefSummery() {
            if (findPreference(getString(R.string.pref_user_class_group_key)) != null) {
                setClassPickerSummery();
                setFilterSelectSummery();
                setNotificationSoundPreference();
            }
        }

        //Notification Sound Preference
        private void setNotificationSoundPreference() {
            String notificationSoundKey = getString(R.string.pref_notification_sound_key);
            String uri = PreferenceUtils.getInstance().getString(R.string.pref_notification_sound_key);
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
            String classPickerKey = getString(R.string.pref_user_class_group_key);
            ClassPickerPreference classPickerPreference =
                    (ClassPickerPreference) findPreference(classPickerKey);

            int classId = classPickerPreference.getValue();
            Realm realm = Realm.getDefaultInstance();

            ClassGroup classGroup = RealmUtils.getGrade(realm, classId);
            String grade = classGroup != null ? classGroup.getName() : "לא הוגדר";
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

        @Override
        public Fragment getCallbackFragment() {
            return this;
        }

        @Override
        public boolean onPreferenceStartScreen(
                PreferenceFragmentCompat preferenceFragmentCompat,
                PreferenceScreen preferenceScreen) {

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
            fragment.setArguments(args);

            ft.add(R.id.fragment, fragment, preferenceScreen.getKey())
                    .addToBackStack(preferenceScreen.getKey())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();

            mCurrentSubscreenKey = preferenceScreen.getKey();
            return true;
        }
    }
}

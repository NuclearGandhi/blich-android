package com.blackcracks.blich.activity;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.preference.ClassPickerPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_forward_white_24dp);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String PREF_CLASS_PICKER_KEY = "class_picker";

        private static List<OnClassPickerPrefChangeListener> sClassPickerListeners = new ArrayList<>();

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
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = super.onCreateView(inflater, container, savedInstanceState);
            if (root != null) {
                ListView list = (ListView) root.findViewById(android.R.id.list);
                list.setPadding(0, 0, 0, 0);
            }
            return root;
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
                    for (OnClassPickerPrefChangeListener listener : sClassPickerListeners) {
                        listener.onClassPickerPrefChanged();
                    }
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

        public static void addClassPickerPrefChangeListener(OnClassPickerPrefChangeListener listener) {
            sClassPickerListeners.add(listener);
        }

        public interface OnClassPickerPrefChangeListener {
            void onClassPickerPrefChanged();
        }
    }
}

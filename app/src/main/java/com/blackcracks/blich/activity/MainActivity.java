/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATECollapsingTbCustomizer;
import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.dialog.ChangelogDialog;
import com.blackcracks.blich.dialog.ClassPickerDialog;
import com.blackcracks.blich.fragment.ExamsFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.util.ClassGroupUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.SyncUtils;
import com.blackcracks.blich.util.Utilities;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import timber.log.Timber;

import static com.blackcracks.blich.dialog.ClassPickerDialog.PREF_IS_FIRST_LAUNCH_KEY;

/**
 * The launch {@link AppCompatActivity}.
 * <p> It instantiates and handles all the necessary settings for the app to work.
 * Realm, old preferences migration, app update handling, and settings the locale to Hebrew - right to left.
 * Handles {@link Fragment} switching and saving it when destroyed.</p>
 */
public class MainActivity extends BaseThemedActivity implements
        ATECollapsingTbCustomizer {

    private static final String FRAGMENT_TAG = "fragment";
    private static final String DIALOG_CLASS_PICKER_TAG = "class_picker";
    private static final String DIALOG_CHANGELOG_TAG = "changelog";

    //Firebase events constants
    private static final String EVENT_CHANGE_FRAGMENT = "change_fragment";
    private static final String EVENT_PARAM_FRAGMENT = "fragment";

    private static final String EVENT_OPEN_ACTIVITY = "open_activity";
    private static final String EVENT_PARAM_ACTIVITY = "activity";

    //Key to store whether this is the first time MainActivity is created in the app process
    private static final String IS_FIRST_INSTANCE_KEY = "is_first";

    private View mRootView;

    private Fragment mFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FirebaseAnalytics mFirebaseAnalytic;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialization stuff
        setupTheme();

        mFirebaseAnalytic = FirebaseAnalytics.getInstance(this);
        FirebaseCrash.setCrashCollectionEnabled(!BuildConfig.DEBUG);

        Timber.plant(new Timber.DebugTree());
        RealmUtils.setUpRealm(this);

        migrateOldSettings();
        setupFirstLaunch(savedInstanceState);

        onUpdate();

        //Link to the layout
        mRootView = LayoutInflater.from(this).inflate(
                R.layout.activity_main, null, false);
        setContentView(mRootView);

        //Restore state if it exists
        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_TAG);
        } else {
            mFragment = new ScheduleFragment();
        }
        replaceFragment(mFragment, false);
        setupBackPress();

        //set up drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        setupDrawer();

        //Create notification channels
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            @SuppressWarnings("ConstantConditions")
            NotificationChannel channel = notificationManager.getNotificationChannel(
                    getString(R.string.notification_channel_schedule_id)
            );

            if (channel == null) {
                createNotificationChannels();
            }
        }
    }

    private void setupTheme() {
        // Default config
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.defaultLightPrimaryColor)
                    .accentColorRes(R.color.defaultLightAccentColor)
                    .coloredNavigationBar(false)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultLightAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultLightAccentColor)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppTheme_Dark)
                    .primaryColorRes(R.color.defaultDarkPrimaryColor)
                    .accentColorRes(R.color.defaultDarkAccentColor)
                    .coloredNavigationBar(false)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultDarkAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultDarkAccentColor)
                    .commit();
        }
    }

    private void migrateOldSettings() {
        //If using old user settings
        if (PreferencesUtils.getInt(this, Preferences.PREF_USER_CLASS_GROUP_KEY) == -1) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, true)
                    .apply();

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(Preferences.getKey(this, Preferences.PREF_USER_CLASS_GROUP_KEY), 1)
                    .apply();
        }
    }

    private void setupFirstLaunch(Bundle savedInstanceState) {

        //Open a class picker dialog in case this is the first time the user opened the app
        boolean isFirstLaunch = Utilities.isFirstLaunch(this);
        if (isFirstLaunch) {
            ClassPickerDialog dialogFragment = null;
            if (savedInstanceState != null) dialogFragment = (ClassPickerDialog)
                    getSupportFragmentManager().findFragmentByTag(DIALOG_CLASS_PICKER_TAG);

            if (dialogFragment == null) {
                dialogFragment = new ClassPickerDialog.Builder()
                        .setDismissible(false)
                        .setDisplayNegativeButton(false)
                        .build();
            }


            dialogFragment.setOnPositiveClickListener(new ClassPickerDialog.OnPositiveClickListener() {
                @Override
                public void onDestroy(Context context, int id) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putInt(Preferences.getKey(context, Preferences.PREF_USER_CLASS_GROUP_KEY), id)
                            .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, false)
                            .apply();
                    SyncUtils.initializeSync(context);
                    showChangelogDialog();
                }
            });

            if (!dialogFragment.isAdded()) {
                dialogFragment.show(getSupportFragmentManager(), DIALOG_CLASS_PICKER_TAG);
            }

        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
                SyncUtils.initializeSync(this);
            }
        }
    }

    private void showChangelogDialog() {
        new ChangelogDialog()
                .show(getSupportFragmentManager(), DIALOG_CHANGELOG_TAG);
    }

    /**
     * Handle app updating.
     */
    private void onUpdate() {
        int oldVersion = PreferencesUtils.getInt(this, Preferences.PREF_APP_VERSION_KEY);
        int newVersion = BuildConfig.VERSION_CODE;
        if (newVersion > oldVersion) {
            if (!Utilities.isFirstLaunch(this)) {//Disable temporarily
                //showChangelogDialog();
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(Preferences.getKey(this, Preferences.PREF_APP_VERSION_KEY),
                            newVersion)
                    .apply();

            if (oldVersion < 36) {
                int id = ClassGroupUtils.getClassValue(this);
                mFirebaseAnalytic.setUserProperty("class_group_id", "" + id);
            }
        }
    }

    private void setupDrawer() {
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.getMenu().findItem(R.id.schedule).setChecked(true);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.isChecked()) return false;
                        mDrawerLayout.closeDrawers();
                        switch (item.getItemId()) {
                            case R.id.schedule: {
                                replaceFragment(new ScheduleFragment(), true);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.exams: {
                                replaceFragment(new ExamsFragment(), true);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.about: {
                                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intent);

                                logOpenActivity(AboutActivity.class);
                                return true;
                            }
                            case R.id.settings: {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);

                                logOpenActivity(SettingsActivity.class);
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );
    }

    private void setupBackPress() {
        getSupportFragmentManager().addOnBackStackChangedListener(
                new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

                        int itemToCheck = 0;
                        if (fragment instanceof ScheduleFragment) {
                            itemToCheck = R.id.schedule;
                        } else if (fragment instanceof ExamsFragment) {
                            itemToCheck = R.id.exams;
                        }
                        if (mFragment != fragment) {
                            logChangeFragment(mFragment.getClass());
                        }
                        mFragment = fragment;

                        mNavigationView.setCheckedItem(itemToCheck);
                    }
                }
        );
    }

    @RequiresApi(api = 26)
    private void createNotificationChannels() {

        NotificationChannel scheduleChannel = new NotificationChannel(
                getString(R.string.notification_channel_schedule_id),
                getString(R.string.notification_channel_schedule_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        scheduleChannel.setDescription(
                getString(R.string.notification_channel_schedule_description)
        );
        scheduleChannel.enableLights(true);
        scheduleChannel.setLightColor(Color.CYAN);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection ConstantConditions
        notificationManager.createNotificationChannel(scheduleChannel);
    }

    /**
     * Replace a fragment.
     *
     * @param fragment       the {@link Fragment} to switch to
     * @param addToBackStack {@code true} add the fragment to back stack.
     */
    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment, FRAGMENT_TAG);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        if (fragment != mFragment) logChangeFragment(mFragment.getClass());
        mFragment = fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_FIRST_INSTANCE_KEY, false);
        //Save fragment
        getSupportFragmentManager().putFragment(outState,
                FRAGMENT_TAG,
                mFragment);
    }

    @Override
    public int getCollapsedTintColor() {
        return Config.getToolbarTitleColor(this, null, getATEKey());
    }

    @Override
    public int getExpandedTintColor() {
        return Config.getToolbarTitleColor(this, null, getATEKey());
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    /**
     * Log change in fragment event to Firebase.
     *
     * @param fragment a {@link Fragment} name.
     */
    private void logChangeFragment(Class fragment) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_PARAM_FRAGMENT, fragment.getSimpleName());
        mFirebaseAnalytic.logEvent(EVENT_CHANGE_FRAGMENT, bundle);
    }

    /**
     * Log open activity event to Firebase
     *
     * @param activity an {@link android.app.Activity} name.
     */
    private void logOpenActivity(Class activity) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_PARAM_ACTIVITY, activity.getSimpleName());
        mFirebaseAnalytic.logEvent(EVENT_OPEN_ACTIVITY, bundle);
    }
}

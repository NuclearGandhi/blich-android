/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.dialog.ClassPickerDialog;
import com.blackcracks.blich.fragment.ExamFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.SyncCallbackUtils;
import com.blackcracks.blich.util.Utilities;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * The launch {@link AppCompatActivity}.
 * <p> It instantiates and handles all the necessary settings for the app to work.
 * Realm, old preferences migration, app update handling, and settings the locale to Hebrew - right to left.
 * Handles {@link Fragment} switching and saving it when destroyed.</p>
 */
public class MainActivity extends BaseThemedActivity {

    private static final String FRAGMENT_TAG = "fragment";
    private static final String DIALOG_CLASS_PICKER_TAG = "class_picker";

    //Firebase events constants
    private static final String EVENT_CHANGE_FRAGMENT = "change_fragment";
    private static final String EVENT_PARAM_FRAGMENT = "fragment";

    private static final String EVENT_OPEN_ACTIVITY = "open_activity";
    private static final String EVENT_PARAM_ACTIVITY = "activity";

    //Key to store whether this is the first time MainActivity is created in the app process
    private static final String IS_FIRST_INSTANCE_KEY = "is_first";

    private FirebaseAnalytics mFirebaseAnalytic;
    private PreferenceUtils mPreferenceUtils;

    private View mRootView;

    private Fragment mFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialization stuff
        setAllowDrawBehindStatusBar();
        setAutoStatusBarColor(false);

        RealmUtils.setUpRealm(this);
        mPreferenceUtils = PreferenceUtils.getInstance(this);
        mFirebaseAnalytic = FirebaseAnalytics.getInstance(this);

        setupFirstLaunch(savedInstanceState);

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
        setupDrawerLayout();
    }

    private void setupFirstLaunch(Bundle savedInstanceState) {

        //Open a class picker dialog in case this is the first time the user opened the app
        boolean isFirstLaunch = mPreferenceUtils.getBoolean(R.string.pref_is_first_launch_key);
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
                    mPreferenceUtils.putInt(R.string.pref_user_class_group_key, id);
                    mPreferenceUtils.putBoolean(R.string.pref_is_first_launch_key, false);
                    SyncCallbackUtils.initializeSync(context);
                    Utilities.showChangelogDialog(getSupportFragmentManager());
                }
            });

            if (!dialogFragment.isAdded()) {
                dialogFragment.show(getSupportFragmentManager(), DIALOG_CLASS_PICKER_TAG);
            }

        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
                SyncCallbackUtils.initializeSync(this);
            }
        }
    }

    private void setupDrawerLayout() {
        mDrawerLayout.setStatusBarBackgroundColor(Config.primaryColorDark(this, getATEKey()));
        setupNavigationView();
    }

    private void setupNavigationView() {
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
                                replaceFragment(new ExamFragment(), true);
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
                        } else if (fragment instanceof ExamFragment) {
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

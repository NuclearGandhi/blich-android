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

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.ExamsFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.sync.BlichSyncUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;
import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = "fragment";

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
        onUpdate();

        //Initialization stuff
        Utilities.setLocaleToHebrew(this);
        RealmUtils.setUpRealm(this);
        Timber.plant(new Timber.DebugTree());
        setupFirstLaunch(savedInstanceState);

        mFirebaseAnalytic = FirebaseAnalytics.getInstance(this);

        //Link to the layout
        mRootView = LayoutInflater.from(this).inflate(
                R.layout.activity_main, null , false);
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
            NotificationChannel channel = notificationManager.getNotificationChannel(
                    getString(R.string.notification_channel_schedule_id)
            );

            if (channel == null) {
                createNotificationChannels();
            }
        }
    }

    /**
     * Save the fragment when the activity is destroyed
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_FIRST_INSTANCE_KEY, false);
        getSupportFragmentManager().putFragment(outState,
                FRAGMENT_TAG,
                mFragment);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    //replace the fragment
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
        notificationManager.createNotificationChannel(scheduleChannel);
    }

    private void setupFirstLaunch(Bundle savedInstanceState) {

        //Open a class picker dialog in case this is the first time the user opened the app
        boolean isFirstLaunch = Utilities.isFirstLaunch(this);
        if (isFirstLaunch) {
            ChooseClassDialogFragment dialogFragment = new ChooseClassDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "choose_class");
            dialogFragment.setOnDestroyListener(new ChooseClassDialogFragment.OnDestroyListener() {
                @Override
                public void onDestroy(Context context) {
                    //Start the periodic sync
                    BlichSyncUtils.initialize(context);
                    Utilities.initializeBlichDataUpdater(context, mRootView);
                }
            });
        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
                BlichSyncUtils.initialize(this);
                Utilities.initializeBlichDataUpdater(this, mRootView);
            }
        }
    }

    private void setupDrawer() {
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.getMenu().getItem(0).setChecked(true);
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
                        if (mFragment != fragment) logChangeFragment(mFragment.getClass());
                        mFragment = fragment;

                        mNavigationView.setCheckedItem(itemToCheck);
                    }
                }
        );
    }

    private void logChangeFragment(Class fragment) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_PARAM_FRAGMENT, fragment.getSimpleName());
        mFirebaseAnalytic.logEvent(EVENT_CHANGE_FRAGMENT, bundle);
    }

    private void logOpenActivity(Class activity) {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_PARAM_ACTIVITY, activity.getSimpleName());
        mFirebaseAnalytic.logEvent(EVENT_OPEN_ACTIVITY, bundle);
    }

    private void onUpdate() {
        if (BuildConfig.VERSION_CODE < 29) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(ChooseClassDialogFragment.PREF_IS_FIRST_LAUNCH_KEY, true)
                    .apply();
        }
    }
}

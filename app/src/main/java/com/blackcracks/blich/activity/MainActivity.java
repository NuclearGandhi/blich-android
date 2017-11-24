package com.blackcracks.blich.activity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.blackcracks.blich.data.BlichDatabase;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.ExamsFragment;
import com.blackcracks.blich.fragment.NewsFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = "fragment";

    //Key to store whether this is the first time MainActivity is created in the app process
    private static final String IS_FIRST_INSTANCE_KEY = "is_first";

    private View mRootView;

    private Fragment mFragment;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocaleToHebrew();
        setupDatabase();
        Timber.plant(new Timber.DebugTree());

        mRootView = LayoutInflater.from(this).inflate(
                R.layout.activity_main, null, false);
        setContentView(mRootView);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_TAG);
        } else {
            mFragment = new ScheduleFragment();
        }
        replaceFragment(mFragment, false);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        setupDrawer();

        boolean isFirstLaunch = Utilities.isFirstLaunch(this);
        if (isFirstLaunch) {
            ChooseClassDialogFragment dialogFragment = new ChooseClassDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "choose_class");
            dialogFragment.setOnDestroyListener(new ChooseClassDialogFragment.OnDestroyListener() {
                @Override
                public void onDestroy(Context context) {
                    //Start the periodic sync
                    BlichSyncAdapter.initializeSyncAdapter(context);
                    Utilities.initializeBlichDataUpdater(context, mRootView);
                }
            });
        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
                Utilities.initializeBlichDataUpdater(this, mRootView);
            }
        }

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
        onUpdate();
    }

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

    private void setLocaleToHebrew() {
        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getApplicationContext().createConfigurationContext(config);

    }

    private void setupDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.isChecked()) return false;
                        mDrawerLayout.closeDrawers();
                        switch (item.getItemId()) {
                            case R.id.schedule: {
                                replaceFragment(new ScheduleFragment(), false);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.exams: {
                                replaceFragment(new ExamsFragment(), false);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.news: {
                                replaceFragment(new NewsFragment(), false);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.about: {
                                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(intent);
                                return true;
                            }
                            case R.id.settings: {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );
    }

    private void setupDatabase() {
        try {
            Manager manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            BlichDatabase.sDatabase = manager.getDatabase(BlichDatabase.DATABASE_NAME);
        } catch (IOException e) {
            //TODO change to proper log
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        com.couchbase.lite.View teacherView = BlichDatabase.sDatabase.getView(BlichDatabase.TEACHER_VIEW_ID);
        if (teacherView.getMap() != null) {
            teacherView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    //Enter data
                    List<Map<String, Object>> data =
                            (List<Map<String, Object>>) document.get(BlichDatabase.SCHEDULE_KEY);

                    Map<String, Object> teachAndSub = new HashMap<>();
                    //iterate each day
                    for (Map<String, Object> day :
                            data) {
                        List<Map<String, Object>> hours =
                                (List<Map<String, Object>>) day.get(BlichDatabase.HOURS_KEY);
                        //iterate each hour
                        for (Map<String, Object> hour :
                                hours) {
                            List<Map<String, Object>> lessons = (List<Map<String, Object>>) hour.get(BlichDatabase.LESSONS_KEY);
                            //iterate each lesson
                            for(Map<String, Object> lesson:
                                    lessons) {

                                String teacher = (String) lesson.get(BlichDatabase.TEACHER_KEY);
                                String subject = (String) lesson.get(BlichDatabase.SUBJECT_KEY);

                                //Emit only teacher-subject pairs that weren't emitted
                                if(!teachAndSub.get(teacher).equals(subject)) {
                                    emitter.emit(teacher, subject);
                                    teachAndSub.put(teacher, subject);
                                }
                            }
                        }
                    }
                }
            },
            "1.0");
        }
    }

    private void onUpdate() {
        if (BuildConfig.VERSION_CODE > 21) {

        }
    }
}

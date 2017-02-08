package com.blackcracks.blich.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.ExamsFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String FRAGMENT_TAG = "fragment";

    //Key to store whether this is the first time MainActivity is created in the app process
    private static final String IS_FIRST_INSTANCE_KEY = "is_first";


    private Fragment mFragment;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Change locale to hebrew
        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().
                    getFragment(savedInstanceState, FRAGMENT_TAG);
        } else {
            mFragment = new ScheduleFragment();
        }
        replaceFragment(mFragment, false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if(item.isChecked()) return false;
                        mDrawerLayout.closeDrawers();
                        switch (item.getItemId()) {
                            case R.id.schedule: {
                                replaceFragment(new ScheduleFragment(), true);
                                item.setChecked(true);
                                return true;
                            }
                            case R.id.settings: {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                return true;
                            }
                            case R.id.exams: {
                                replaceFragment(new ExamsFragment(), true);
                                item.setChecked(true);
                                return true;
                            }
                        }
                        return false;
                    }
                }
        );

        boolean isFirstLaunch = Utilities.isFirstLaunch(this);
        if (isFirstLaunch) {
            DialogFragment dialogFragment = new ChooseClassDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "choose_class");
        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
                BlichSyncAdapter.syncImmediately(this);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_FIRST_INSTANCE_KEY, false);
        getSupportFragmentManager().putFragment(outState,
                FRAGMENT_TAG,
                mFragment);
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

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }
}

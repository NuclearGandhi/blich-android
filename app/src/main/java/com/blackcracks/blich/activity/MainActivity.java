package com.blackcracks.blich.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.blackcracks.blich.R;
import com.blackcracks.blich.fragment.ChooseClassDialogFragment;
import com.blackcracks.blich.fragment.ScheduleFragment;
import com.blackcracks.blich.fragment.SettingsFragment;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String FRAGMENT_TAG = "schedule_fragment";

    private static final String IS_FIRST_INSTANCE_KEY = "is_first";

    private Fragment mFragment;
    private View mToolbarElevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale locale = new Locale("iw");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        Utilities.initializeUtils();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mToolbarElevation = findViewById(R.id.toolbar_elevation);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().
                    getFragment(savedInstanceState, FRAGMENT_TAG);
        } else {
            mFragment = new ScheduleFragment();
        }

        replaceFragment(mFragment, false);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,R.string.drawer_open_desc, R.string.drawer_close_desc) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        if(!item.isChecked()) item.setChecked(true);
                        else return false;
                        drawerLayout.closeDrawers();
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case R.id.schedule: {
                                fragment = new ScheduleFragment();
                                mToolbarElevation.setVisibility(View.GONE);
                                break;
                            }
                            case R.id.settings: {
                                fragment = new SettingsFragment();
                                mToolbarElevation.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                        if (fragment != null) {
                            replaceFragment(fragment, false);
                            return true;
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
            BlichSyncAdapter.initializeSyncAdapter(this);
            if (savedInstanceState == null) {
                BlichSyncAdapter.syncImmediately(this);
            } else if (!savedInstanceState.containsKey(IS_FIRST_INSTANCE_KEY)) {
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
}

package com.blackcracks.blich.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;

/**
 * The ScheduleFragment class is responsible for getting and displaying the schedule
 * of the user
 */
public class ScheduleFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String LOG_TAG = ScheduleFragment.class.getSimpleName();

    private CoordinatorLayout mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver mSyncBroadcastReceiver;

    public ScheduleFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSyncBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @BlichSyncAdapter.FetchStatus int status =
                        intent.getIntExtra(BlichSyncAdapter.FETCH_STATUS,
                        BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);
                Utilities.onSyncFinished(context, mRootView, status);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = (CoordinatorLayout) inflater.inflate(R.layout.fragment_schedule, container, false);

        TabLayout tabLayout = (TabLayout) mRootView.findViewById(R.id.tablayout_schedule_days);
        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.viewpager_schedule);
        if (viewPager != null) {
            viewPager.setAdapter(
                    new SchedulePagerAdapter(getActivity().getSupportFragmentManager(),
                            getResources().getStringArray(R.array.tab_schedule_names)));

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour > 18) day = day % 7 + 1;
            switch (day) {
                case 1: {
                    day = 0;
                    break;
                }
                case 2: {
                    day = 1;
                    break;
                }
                case 3: {
                    day = 2;
                    break;
                }
                case 4: {
                    day = 3;
                    break;
                }
                case 5: {
                    day = 4;
                    break;
                }
                case 6: {
                    day = 5;
                    break;
                }
                case 7: {
                    day = 0;
                    break;
                }
            }
            viewPager.setCurrentItem(SchedulePagerAdapter.getRealPosition(day), false);

        }
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        mSwipeRefreshLayout =
                (SwipeRefreshLayout) mRootView.findViewById(R.id.swiperefresh_schedule);
        mSwipeRefreshLayout.setEnabled(false);

        return mRootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(R.string.drawer_schedule_title);

        DrawerLayout drawerLayout = activity.getDrawerLayout();
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar,R.string.drawer_open_desc, R.string.drawer_close_desc) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                Utilities.updateBlichData(getContext(), mRootView);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(BlichSyncAdapter.ACTION_SYNC_FINISHED));
        mSwipeRefreshLayout.setRefreshing(Utilities.getPreferenceBoolean(getContext(), getString(R.string.pref_is_fetching_key), true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mSyncBroadcastReceiver);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_is_fetching_key))) {
            mSwipeRefreshLayout.setRefreshing(sharedPreferences.getBoolean(getString(R.string.pref_is_fetching_key), true));
        }
    }
}

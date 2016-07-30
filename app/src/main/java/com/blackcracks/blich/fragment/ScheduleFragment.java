package com.blackcracks.blich.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.SettingsActivity;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.data.FetchBlichData;
import com.blackcracks.blich.data.FetchScheduleData;

import java.util.Calendar;


public class ScheduleFragment extends Fragment implements
        SettingsActivity.SettingsFragment.OnClassPickerPrefChangeListener,
        FetchBlichData.OnFetchFinishListener {

    private static final String NEW_DATA_KEY = "new_data";

    private boolean mShouldRefresh = true;
    private CoordinatorLayout mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ChooseClassDialogFragment mDialogFragment;

    public ScheduleFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = (CoordinatorLayout) inflater.inflate(R.layout.fragment_schedule, container, false);
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout) mRootView.findViewById(R.id.tablayout_schedule_days);

        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.viewpager_schedule);
        if (viewPager != null) {
            viewPager.setAdapter(
                    new SchedulePagerAdapter(getActivity().getSupportFragmentManager(),
                            getResources().getStringArray(R.array.tab_schedule_names)));

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            switch (day) {
                case Calendar.SUNDAY: {
                    day = 0;
                    break;
                }
                case Calendar.MONDAY: {
                    day = 1;
                    break;
                }
                case Calendar.TUESDAY: {
                    day = 2;
                    break;
                }
                case Calendar.WEDNESDAY: {
                    day = 3;
                    break;
                }
                case Calendar.THURSDAY: {
                    day = 4;
                    break;
                }
                case Calendar.FRIDAY: {
                    day = 5;
                    break;
                }
                default: {
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
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSchedule();
            }
        });
        mSwipeRefreshLayout.setEnabled(false);

        boolean isFirstLaunch = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(ChooseClassDialogFragment.PREF_IS_FIRST_LAUNCH_KEY,
                        true);
        if (isFirstLaunch) {
            mShouldRefresh = false;
            mDialogFragment = new ChooseClassDialogFragment();
            mDialogFragment.show(getActivity().getSupportFragmentManager(), "choose_class");
        }

        SettingsActivity.SettingsFragment.addClassPickerPrefChangeListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mShouldRefresh = savedInstanceState.getBoolean(NEW_DATA_KEY);
        }
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
                refreshSchedule();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDialogFragment != null) {
            mDialogFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    refreshSchedule();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShouldRefresh) {
            refreshSchedule();
            mShouldRefresh = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEW_DATA_KEY, mShouldRefresh);
    }

    @Override
    public void onClassPickerPrefChanged() {
        mShouldRefresh = true;
    }

    @Override
    public void onFetchFinished(boolean isSuccessful) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (isSuccessful) {
            Snackbar.make(mRootView,
                    R.string.snackbar_schedule_fetch_success,
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_connection,
                    null);
            TextView message = (TextView) view.findViewById(R.id.dialog_message);
            message.setText(R.string.dialog_schedule_fetch_failed);
            new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(R.string.dialog_no_connection_try_again,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refreshSchedule();
                                }
                            })
                    .setNegativeButton(R.string.dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
        }
    }

    public void refreshSchedule() {
        mSwipeRefreshLayout.setRefreshing(true);
        new FetchScheduleData(getContext())
                .addOnFetchFinishListener(this)
                .execute();
    }


}

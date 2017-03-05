package com.blackcracks.blich.fragment;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.activity.SettingsActivity;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;

/**
 * The ScheduleFragment class is responsible for getting and displaying the schedule
 * of the user
 */
public class ScheduleFragment extends Fragment {

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
                @BlichSyncAdapter.FetchStatus int response =
                        intent.getIntExtra(BlichSyncAdapter.FETCH_STATUS,
                        BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);
                onSyncFinished(response);
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
        if (Utilities.isFirstLaunch(getContext())) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

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
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(BlichSyncAdapter.ACTION_SYNC_FINISHED));
        if (!Utilities.isFirstLaunch(getContext())) {
            new GetCurrentClassTask().execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mSyncBroadcastReceiver);
    }

    private void onSyncFinished(@BlichSyncAdapter.FetchStatus int status) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (status == BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(mRootView,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncAdapter.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncAdapter.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = (TextView) view.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = (TextView) view.findViewById(R.id.dialog_message);
            message.setText(messageString);

            new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(R.string.dialog_try_again,
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

    private void refreshSchedule() {
        Log.d(LOG_TAG, "Refreshing");
        mSwipeRefreshLayout.setRefreshing(true);
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            BlichSyncAdapter.syncImmediately(getContext());
        } else {
            onSyncFinished(BlichSyncAdapter.FETCH_STATUS_NO_CONNECTION);
        }
    }

    private class GetCurrentClassTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            Cursor scheduleCursor = getContext().getContentResolver().query(
                    ScheduleEntry.CONTENT_URI,
                    new String[]{ScheduleEntry.COL_CLASS_SETTINGS},
                    null,
                    null,
                    ScheduleEntry.COL_DAY + " LIMIT 1");

            String classIndex = "";

            if (scheduleCursor != null && scheduleCursor.moveToFirst()) {
                classIndex = scheduleCursor.getString(scheduleCursor
                        .getColumnIndex(ScheduleEntry.COL_CLASS_SETTINGS));
                scheduleCursor.close();
            }

            Cursor classCursor = getContext().getContentResolver().query(
                    ClassEntry.CONTENT_URI,
                    new String[] {ClassEntry.COL_GRADE, ClassEntry.COL_GRADE_INDEX},
                    ClassEntry.COL_CLASS_INDEX + " = ?",
                    new String[] {classIndex},
                    null);

            String currentClass = "";

            if (classCursor != null && classCursor.moveToFirst()) {
                String gradeName = classCursor.getString(classCursor.getColumnIndex(ClassEntry.COL_GRADE));
                int gradeIndex = classCursor.getInt(classCursor.getColumnIndex(ClassEntry.COL_GRADE_INDEX));
                if (gradeIndex == 0) {
                    currentClass = gradeName;
                } else {
                    currentClass = gradeName + "'" + gradeIndex;
                }
                classCursor.close();
            }

            String classSettings = Utilities.getPreferenceString(getContext(),
                    SettingsActivity.SettingsFragment.PREF_CLASS_PICKER_KEY,
                    SettingsActivity.SettingsFragment.PREF_CLASS_PICKER_DEFAULT,
                    false);
            return !currentClass.equals(classSettings);
        }

        @Override
        protected void onPostExecute(Boolean didClassChange) {
            if (didClassChange) {
                refreshSchedule();
            }
        }
    }
}

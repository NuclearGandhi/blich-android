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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.data.BlichContract.ClassEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.PrefUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;


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
                boolean isSuccessful = intent.getBooleanExtra(BlichSyncAdapter.IS_SUCCESSFUL_EXTRA, false);
                mSwipeRefreshLayout.setRefreshing(false);
                if (isSuccessful) {
                    Snackbar.make(mRootView,
                            R.string.snackbar_schedule_fetch_success,
                            Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    onSyncFailed();
                }
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.drawer_schedule_title);
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
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(BlichSyncAdapter.ACTION_SYNC_FINISHED));
        if (!Utilities.isFirstLaunch(getContext())) {
            new GetCurrentClassTask().execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mSyncBroadcastReceiver);
    }

    public void onSyncFailed() {
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.dialog_no_connection,
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

    public void refreshSchedule() {
        Log.d(LOG_TAG, "Refreshing");
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            BlichSyncAdapter.syncImmediately(getContext());
        } else {
            onSyncFailed();
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
                currentClass = classCursor.getString(classCursor
                        .getColumnIndex(ClassEntry.COL_GRADE)) + "'/" +
                        classCursor.getInt(classCursor
                        .getColumnIndex(ClassEntry.COL_GRADE_INDEX));
                Log.d(LOG_TAG, currentClass);
                classCursor.close();
            }

            String classSettings = PrefUtils.getClassSettings(getContext());
            Log.d(LOG_TAG, classSettings);
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

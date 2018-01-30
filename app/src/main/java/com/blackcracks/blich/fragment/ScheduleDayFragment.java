package com.blackcracks.blich.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ScheduleAdapter;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.RealmUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<ScheduleResult>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DAY_KEY = "day";

    private static final int SCHEDULE_LOADER_ID = 1;

    private ScheduleAdapter mAdapter;

    private Realm mRealm;
    private RealmChangeListener mChangeListener;
    private int mDay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments() != null ? getArguments().getInt(DAY_KEY) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        setUpRefresher();

        View rootView = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        TextView statusTextView = rootView.findViewById(R.id.text_status);

        ExpandableListView listView = rootView.findViewById(R.id.expandable_listview_schedule_day);
        mAdapter = new ScheduleAdapter(listView, getContext(), statusTextView);
        listView.setAdapter(mAdapter);
        listView.setChildDivider(
                ContextCompat.getDrawable(getContext(),
                        android.R.color.transparent)); //Hide the child dividers

        ViewCompat.setNestedScrollingEnabled(listView, true);
        return rootView;
    }

    private void setUpRefresher() {
        mChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                if (isAdded()) getLoaderManager().restartLoader(
                        SCHEDULE_LOADER_ID,
                        Bundle.EMPTY,
                        ScheduleDayFragment.this);
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, Bundle.EMPTY, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRealm.addChangeListener(mChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(mChangeListener);
        mRealm.close();
    }

    @Override
    public Loader<ScheduleResult> onCreateLoader(int id, Bundle args) {
        return new ScheduleLoader(getContext(), mDay, mRealm);
    }

    @Override
    public void onLoadFinished(Loader<ScheduleResult> loader, ScheduleResult data) {
        mAdapter.switchData(data);
    }

    @Override
    public void onLoaderReset(Loader<ScheduleResult> loader) {
        mAdapter.switchData(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY))) {
            getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, Bundle.EMPTY, this);
        }
    }

    private static class ScheduleLoader extends Loader<ScheduleResult> {

        private int mDay;

        private Realm mRealm;

        public ScheduleLoader(Context context, int day, Realm realm) {
            super(context);
            mDay = day;
            mRealm = realm;
        }

        /**
         * Query the schedule data and filter it if need
         */
        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            if (mRealm.isClosed()) return;

            //Check if the user wants to filter the schedule
            boolean isFilterOn = PreferencesUtils.getBoolean(
                    getContext(),
                    Preferences.PREF_FILTER_TOGGLE_KEY);

            List<Hour> hours;
            List<Change> changes;
            List<Event> events;

            if (isFilterOn) { //Filter
                //Query using Inverse-Relationship and filter
                RealmResults<Lesson> lessons = RealmUtils.buildFilteredQuery(
                        mRealm,
                        getContext(),
                        Lesson.class,
                        mDay)
                        .findAll();

                hours = RealmUtils.convertLessonListToHour(lessons, mDay);
                Collections.sort(hours);

                changes = RealmUtils.buildFilteredQuery(
                        mRealm,
                        getContext(),
                        Change.class,
                        mDay)
                        .findAll();

                events = RealmUtils.buildFilteredQuery(
                        mRealm,
                        getContext(),
                        Event.class,
                        mDay)
                        .findAll();

            } else {//No filter, Query all
                RealmResults<Hour> hourList = mRealm.where(Hour.class)
                        .equalTo("day", mDay)
                        .findAll()
                        .sort("hour", Sort.ASCENDING);

                hours = new ArrayList<>(hourList);

                changes = RealmUtils.buildBaseQuery(
                        mRealm,
                        Change.class,
                        mDay)
                        .findAll();

                events = RealmUtils.buildBaseQuery(
                        mRealm,
                        Event.class,
                        mDay)
                        .findAll();
            }





            ScheduleResult result = new ScheduleResult(hours, changes, events);
            deliverResult(result);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }
}

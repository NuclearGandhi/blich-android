package com.blackcracks.blich.fragment;

import android.content.Context;
import android.os.Bundle;
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
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Hour>>{

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
                if (!isDetached()) getLoaderManager().getLoader(SCHEDULE_LOADER_ID).onContentChanged();
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, Bundle.EMPTY, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRealm.addChangeListener(mChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(mChangeListener);
        mRealm.close();
    }

    @Override
    public Loader<List<Hour>> onCreateLoader(int id, Bundle args) {
        return new ScheduleLoader(getContext(), mDay, mRealm);
    }

    @Override
    public void onLoadFinished(Loader<List<Hour>> loader, List<Hour> data) {
        mAdapter.switchData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Hour>> loader) {
        mAdapter.switchData(null);
    }

    private static class ScheduleLoader extends Loader<List<Hour>> {

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

            //Check if the user wants to filter the schedule
            boolean isFilterOn = Utilities.getPrefBoolean(
                    getContext(),
                    Constants.Preferences.PREF_FILTER_TOGGLE_KEY);

            List<Hour> results;

            if (isFilterOn) { //Filter
                //Query using Inverse-Relationship and filter
                RealmResults<Lesson> lessons = getFilteredLessonsQuery()
                        .findAll();

                //Translate the lesson list to hour list
                results = new ArrayList<>();
                for (Lesson lesson :
                        lessons) {
                    int hourNum = lesson.getOwners().get(0).getHour();
                    Hour hour = null;

                    for (Hour result :
                            results) {
                        if (result.getHour() == hourNum) hour = result;
                    }

                    if (hour == null) {
                        RealmList<Lesson> lessonList = new RealmList<>();
                        lessonList.add(lesson);
                        hour = new Hour(mDay, hourNum, lessonList);
                        results.add(hour);
                    } else {
                        hour.getLessons().add(lesson);
                    }
                }

                //Sort the hours
                Comparator<Hour> hourComparator = new Comparator<Hour>() {
                    @Override
                    public int compare(Hour o1, Hour o2) {
                        if (o1.getHour() > o2.getHour()) return 1;
                        else if (o1.getHour() == o2.getHour()) return 0;
                        else return -1;
                    }
                };

                Collections.sort(results, hourComparator);

            } else {//No filter, Query all
                results = mRealm.where(Hour.class)
                        .equalTo("day", mDay)
                        .findAll()
                        .sort("hour", Sort.ASCENDING);
            }

            deliverResult(results);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }

        /**
         * Get a query object that contains all the filter rules
         * @return {@link RealmQuery} object with filter rules
         */
        private RealmQuery<Lesson> getFilteredLessonsQuery() {
            String teacherFilter = Utilities.getPrefString(
                    getContext(),
                    Constants.Preferences.PREF_FILTER_SELECT_KEY);
            String[] teacherSubjects = teacherFilter.split(";");

            RealmQuery<Lesson> lessons = mRealm.where(Lesson.class)
                    .equalTo("owners.day", mDay) //Inverse Relationship
                    .and()
                    .beginGroup()
                        //Lessons with empty teacher are changes
                        .equalTo("teacher", " ");

            for (String teacherSubject :
                    teacherSubjects) {
                if (teacherSubject.equals("")) break;

                String[] arr = teacherSubject.split(",");
                String teacher = arr[0];
                String subject = arr[1];

                lessons.or()
                        .beginGroup()
                            .equalTo("teacher", teacher)
                            .and()
                            .equalTo("subject", subject)
                        .endGroup();
            }

            lessons.endGroup();

            return lessons;
        }
    }
}

package com.blackcracks.blich.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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
import com.blackcracks.blich.data.BlichDatabase;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<QueryEnumerator> {

    public static final String DAY_KEY = "day";

    private static final int SCHEDULE_LOADER_ID = 1;

    private ScheduleAdapter mAdapter;
    private int mDay;

    private LiveQuery mLiveQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments() != null ? getArguments().getInt(DAY_KEY) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        TextView statusTextView = rootView.findViewById(R.id.text_status);

        ExpandableListView listView = rootView.findViewById(R.id.expandable_listview_schedule_day);
        mAdapter = new ScheduleAdapter(listView, getContext(), statusTextView);
        listView.setAdapter(mAdapter);
        listView.setChildDivider(
                ContextCompat.getDrawable(getContext(),
                        android.R.color.transparent)); //Hide the child dividers

        ViewCompat.setNestedScrollingEnabled(listView, true);

        setUpCouchbaseView();
        setUpLiveQuery();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SCHEDULE_LOADER_ID, null, this);
    }

    private void setUpCouchbaseView() {
        com.couchbase.lite.View scheduleView = BlichDatabase.sDatabase.getView(
                BlichDatabase.SCHEDULE_VIEW_ID + mDay);
        if (scheduleView.getMap() == null) {
            scheduleView.setMap(
                    new Mapper() {
                        @Override
                        public void map(Map<String, Object> document, Emitter emitter) {
                            List<Map<String, Object>> data =
                                    (List<Map<String, Object>>) document.get(BlichDatabase.SCHEDULE_KEY);
                            Map<String, Object> day = data.get(mDay);

                            List<Map<String, Object>> hours =
                                    (List<Map<String, Object>>) day.get(BlichDatabase.HOURS_KEY);

                            //Iterate through each hour
                            for (Map<String, Object> hour :
                                    hours) {
                                List<Map<String, Object>> lessons = (List<Map<String, Object>>) hour.get(BlichDatabase.LESSONS_KEY);


                                //Add filter
                                List<Map<String, Object>> filtered = new ArrayList<>();

                                boolean isFilter = Utilities.getPrefBoolean(
                                        getContext(),
                                        Constants.Preferences.getKey(
                                                getContext(),
                                                Constants.Preferences.PREF_FILTER_TOGGLE_KEY),
                                        (Boolean) Constants.Preferences.getDefault(
                                                getContext(),
                                                Constants.Preferences.PREF_FILTER_TOGGLE_KEY
                                        )
                                );

                                if (isFilter) {
                                    for (Map<String, Object> lesson :
                                            lessons) {
                                        String teacher = (String) lesson.get(BlichDatabase.TEACHER_KEY);
                                        String subject = (String) lesson.get(BlichDatabase.SUBJECT_KEY);
                                        if (Utilities.Nosql.filterString(getContext(), teacher, subject)) {
                                            filtered.add(lesson);
                                        }
                                    }
                                } else {
                                    filtered = lessons;
                                }

                                if (filtered.size() != 0) {
                                    emitter.emit(BlichDatabase.HOUR_KEY + hour.get(BlichDatabase.HOUR_KEY), filtered);
                                }
                            }
                        }
                    },
                    "1.0");
        }
    }

    private void setUpLiveQuery() {
        mLiveQuery = BlichDatabase.sDatabase.getView(BlichDatabase.SCHEDULE_VIEW_ID + mDay)
                .createQuery()
                .toLiveQuery();
        mLiveQuery.addChangeListener(
                new LiveQuery.ChangeListener() {
                    @Override
                    public void changed(LiveQuery.ChangeEvent event) {
                        getLoaderManager().getLoader(SCHEDULE_LOADER_ID)
                                .onContentChanged();
                    }
                }
        );
        mLiveQuery.start();
    }

    @Override
    public Loader<QueryEnumerator> onCreateLoader(int id, Bundle args) {
        return new ScheduleLoader(getContext(), mDay);
    }

    @Override
    public void onLoadFinished(Loader<QueryEnumerator> loader, QueryEnumerator data) {
        mAdapter.switchData(data);
    }

    @Override
    public void onLoaderReset(Loader<QueryEnumerator> loader) {
        mAdapter.switchData(null);
    }

    static class ScheduleLoader extends AsyncTaskLoader<QueryEnumerator> {

        int mDay;

        public ScheduleLoader(Context context, int day) {
            super(context);
            mDay = day;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (takeContentChanged()) {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }

        @Override
        public QueryEnumerator loadInBackground() {
            try {
                Query query = BlichDatabase.sDatabase
                        .getView(BlichDatabase.SCHEDULE_VIEW_ID + mDay)
                        .createQuery();
                return query.run();
            } catch (CouchbaseLiteException e) {
                Timber.e(e);
            }
            return null;
        }
    }
}

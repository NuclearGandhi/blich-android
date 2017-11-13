package com.blackcracks.blich.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ScheduleAdapter;
import com.blackcracks.blich.data.BlichDatabase;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;

import java.util.List;
import java.util.Map;

/**
 * The ScheduleDayFragment is the fragment in each one of the pages of the ScheduleFragment
 */
public class ScheduleDayFragment extends Fragment {

    public static final String DAY_KEY = "day";

    private ScheduleAdapter mAdapter;
    private int mDay;
    private TextView mStatusTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments() != null ? getArguments().getInt(DAY_KEY) : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        ExpandableListView listView = rootView.findViewById(R.id.expandable_listview_schedule_day);
        mAdapter = new ScheduleAdapter(getContext(), mDay);
        listView.setAdapter(mAdapter);
        listView.setChildDivider(
                ContextCompat.getDrawable(getContext(),
                        android.R.color.transparent)); //Hide the child dividers

        ViewCompat.setNestedScrollingEnabled(listView, true);

        mStatusTextView = rootView.findViewById(R.id.text_status);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setUpCouchbaseView() {
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
                            for (Map<String, Object> hour :
                                    hours) {
                                emitter.emit(
                                        BlichDatabase.HOUR_KEY + mDay, hour.get(BlichDatabase.LESSONS_KEY));
                            }
                        }
                    },
                    "1.0");
        }
    }

    class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private QueryEnumerator mData;

        @Override
        protected Void doInBackground(Void... voids) {
            QueryEnumerator data = null;
            try {
                Query query = BlichDatabase.sDatabase.getView(
                        BlichDatabase.SCHEDULE_VIEW_ID + mDay).createQuery();
                data = query.run();
            } catch (CouchbaseLiteException e) {
                //TODO change to proper log
                e.printStackTrace();
            }

            if (data != null) {
                mData = data;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.setData(mData);
        }
    }
}

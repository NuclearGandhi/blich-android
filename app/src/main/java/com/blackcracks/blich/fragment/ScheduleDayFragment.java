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

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
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

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            RealmResults<Hour> results = mRealm.where(Hour.class)
                    .equalTo("day", mDay)
                    .findAll();
            results.sort("hour", Sort.ASCENDING);

            deliverResult(results);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }
}

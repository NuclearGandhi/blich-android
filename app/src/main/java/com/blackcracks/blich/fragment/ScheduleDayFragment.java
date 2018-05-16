/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ScheduleAdapter;
import com.blackcracks.blich.data.schedule.Period;
import com.blackcracks.blich.util.ScheduleUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * {@link ScheduleDayFragment} is the fragment in each one of the pages of {@link ScheduleFragment}.
 * It contains a list of hour for the specified day of the week.
 */
@SuppressWarnings("ConstantConditions")
public class ScheduleDayFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<Period>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DAY_KEY = "day";

    private static final int SCHEDULE_LOADER_ID = 1;

    private RecyclerView mRecyclerView;
    private ScheduleAdapter mAdapter;

    private Realm mRealm;
    private RealmChangeListener<Realm> mChangeListener;
    private int mDay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments() != null ? getArguments().getInt(DAY_KEY) : 1;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        setUpRefresher();

        View rootView = inflater.inflate(R.layout.fragment_schedule_day, container, false);

        //TODO update text accordingly
        TextView statusTextView = rootView.findViewById(R.id.text_status);

        mRecyclerView = rootView.findViewById(R.id.recycler_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getContext(),
                DividerItemDecoration.HORIZONTAL
        ));

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, true);
        return rootView;
    }

    private void setUpRefresher() {
        mChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(@NonNull Realm realm) {
                if (isAdded()) {
                    getLoaderManager().restartLoader(
                            SCHEDULE_LOADER_ID,
                            Bundle.EMPTY,
                            ScheduleDayFragment.this);
                }
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
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.removeChangeListener(mChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearData();
        mRealm.close();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getContext() == null) return; //In some cases this method is called when the fragment has been detached

        if (key.equals(getString(R.string.pref_filter_toggle_key))) {
            getLoaderManager().restartLoader(SCHEDULE_LOADER_ID, Bundle.EMPTY, this);
        }
    }

    @Override
    public Loader<List<Period>> onCreateLoader(int id, Bundle args) {
        return new ScheduleLoader(getContext(), mDay);
    }

    @Override
    public void onLoadFinished(Loader<List<Period>> loader, List<Period> data) {
        clearData();
        mAdapter = new ScheduleAdapter(
                data,
                getContext()
        );
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Period>> loader) {
        clearData();
        mRecyclerView.setAdapter(null);
    }

    private void clearData() {
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }

    /**
     * A {@link Loader< List<Period> >} to fetch schedule from {@link Realm} database
     */
    private static class ScheduleLoader extends AsyncTaskLoader<List<Period>> {

        private int mDay;

        public ScheduleLoader(Context context, int day) {
            super(context);
            mDay = day;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }

        @Nullable
        @Override
        public List<Period> loadInBackground() {
            return ScheduleUtils.fetchProcessedData(mDay);
        }
    }
}
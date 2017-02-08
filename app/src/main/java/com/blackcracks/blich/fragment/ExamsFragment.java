package com.blackcracks.blich.fragment;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichContract.*;

public class ExamsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXAMS_LOADER_ID = 101;

    private static final String[] EXAMS_COLUMNS = {
            ExamsEntry._ID,
            ExamsEntry.COL_DATE,
            ExamsEntry.COL_SUBJECT,
            ExamsEntry.COL_TEACHER
    };

    Context mContext;

    View mRootView;
    RecyclerView mRecyclerView;
    ExamAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_exams, container, false);
        mContext = getContext();

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.exam_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ExamAdapter(mContext, null);
        mRecyclerView.setAdapter(mAdapter);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(R.string.drawer_exams_title);

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


        getLoaderManager().initLoader(EXAMS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = BlichContract.ExamsEntry.CONTENT_URI;

        return new CursorLoader(
                mContext,
                uri,
                EXAMS_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
}

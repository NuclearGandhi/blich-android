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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
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
    RecyclerView mRecyclerView;
    ExamAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXAMS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exams, container, false);
        mContext = getContext();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.exam_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ExamAdapter(mContext, null);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
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

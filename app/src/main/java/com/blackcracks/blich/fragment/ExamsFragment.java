package com.blackcracks.blich.fragment;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

public class ExamsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    Context mContext;
    RecyclerView mRecyclerView;
    ExamAdapter mAdapter;

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
        Uri uri = BlichContract.ClassEntry.CONTENT_URI;

        return new CursorLoader(
                mContext,
                uri,
                null, null, null, null);
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

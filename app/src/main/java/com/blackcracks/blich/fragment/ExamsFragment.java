package com.blackcracks.blich.fragment;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.util.Utilities;

public class ExamsFragment extends BlichBaseFragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
        mRootView = super.onCreateView(inflater, container, savedInstanceState);
        mContext = getContext();

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.exam_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ExamAdapter(mContext, null);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXAMS_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                Utilities.updateBlichData(getContext(), mRootView);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_exams;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.drawer_exams_title;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_schedule;
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

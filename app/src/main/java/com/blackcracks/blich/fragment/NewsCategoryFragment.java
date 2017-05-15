package com.blackcracks.blich.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.NewsAdapter;
import com.blackcracks.blich.data.BlichContract.NewsEntry;
import com.blackcracks.blich.data.FetchNewsService;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

public class NewsCategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String KEY_CATEGORY = "category";

    private NewsAdapter mAdapter;
    private int mCategory;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = getArguments() != null ? getArguments().getInt(KEY_CATEGORY) : 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_news_category, container);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mAdapter = new NewsAdapter(getContext(), null);
        recyclerView.setAdapter(mAdapter);

        Button button = (Button) rootView.findViewById(R.id.refresh_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FetchNewsService.class);
                intent.putExtra(Constants.IntentConstants.EXTRA_NEWS_CATEGORY, mCategory);
                getContext().startService(intent);
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @BlichSyncAdapter.FetchStatus int status = intent.getIntExtra(Constants.IntentConstants.EXTRA_FETCH_STATUS,
                        BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);
                Utilities.onSyncFinished(getContext(), rootView, status);
            }
        };

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(Constants.NEWS_LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.IntentConstants.ACTION_FETCH_NEWS_CALLBACK));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NewsEntry.COL_TITLE,
                NewsEntry.COL_BODY,
                NewsEntry.COL_DATE,
                NewsEntry.COL_AUTHOR};

        String selection = NewsEntry.COL_CATEGORY + " = " + mCategory;
        Uri uri = NewsEntry.CONTENT_URI;

        return new CursorLoader(
                getContext(),
                uri,
                projection,
                selection,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}

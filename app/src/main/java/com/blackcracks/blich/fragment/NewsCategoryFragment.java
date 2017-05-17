package com.blackcracks.blich.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.NewsAdapter;
import com.blackcracks.blich.data.BlichContract.NewsEntry;
import com.blackcracks.blich.data.FetchNewsService;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Constants;

public class NewsCategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String KEY_CATEGORY = "category";

    private int mCategory;

    private View mRootView;
    private View mRefreshImage;
    private ProgressBar mRefreshProgressBar;

    private NewsAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = getArguments() != null ? getArguments().getInt(KEY_CATEGORY) : 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View mRootView = inflater.inflate(R.layout.fragment_news_category, container, false);

        final RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        mAdapter = new NewsAdapter(getContext(), null);
        recyclerView.setAdapter(mAdapter);

        mRefreshImage = mRootView.findViewById(R.id.refresh_image);
        mRefreshProgressBar = (ProgressBar) mRootView.findViewById(R.id.refresh_progress_bar);
        mRefreshProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        View refreshButton = mRootView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        final View refreshView = mRootView.findViewById(R.id.refresh_view);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Callback
                @BlichSyncAdapter.FetchStatus int status = intent.getIntExtra(Constants.IntentConstants.EXTRA_FETCH_STATUS,
                        BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);
                onFetchFinished(getContext(), status);

                if (status == BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL) {
                    recyclerView.setVisibility(View.VISIBLE);
                    refreshView.setVisibility(View.GONE);
                }
            }
        };

        return mRootView;
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
                NewsEntry._ID,
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

    private void refresh() {
        //Start the refresh animation
        mRefreshImage.setVisibility(View.GONE);
        mRefreshProgressBar.setVisibility(View.VISIBLE);

        //Call the fetch news service
        Intent intent = new Intent(getContext(), FetchNewsService.class);
        intent.putExtra(Constants.IntentConstants.EXTRA_NEWS_CATEGORY, mCategory);
        getContext().startService(intent);
    }

    //Callback from FetchNewsService
    private void onFetchFinished(final Context context, @BlichSyncAdapter.FetchStatus int status) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(context.getString(R.string.pref_is_syncing_key), false)
                .apply();

        if (status == BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(mRootView,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncAdapter.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncAdapter.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = (TextView) dialogView.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = (TextView) dialogView.findViewById(R.id.dialog_message);
            message.setText(messageString);

            new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_try_again,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refresh();
                                }
                            })
                    .setNegativeButton(R.string.dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
        }
    }

}

package com.blackcracks.blich.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.NewsAdapter;
import com.blackcracks.blich.data.BlichContract.NewsEntry;
import com.blackcracks.blich.sync.FetchNewsService;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.NewsUtils;

import java.lang.reflect.Field;

import timber.log.Timber;

public class NewsCategoryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = NewsCategoryFragment.class.getSimpleName();

    public static final String KEY_CATEGORY = "category";
    private int mCategory;

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private NewsAdapter mAdapter;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = getArguments() != null ? getArguments().getInt(KEY_CATEGORY) : 0;

        NewsUtils.resetIsFetchingPreferences(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_news_category, container, false);

        RecyclerView recyclerView = mRootView.findViewById(R.id.recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getContext(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new NewsAdapter(getContext(), null);
        recyclerView.setAdapter(mAdapter);

        mBroadcastReceiver = new StatusBroadcastReceiver();

        mSwipeRefreshLayout =
                mRootView.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });


        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(Constants.NEWS_LOADER_ID, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(NewsUtils.getActionForCategory(mCategory)));
        mSwipeRefreshLayout.setRefreshing(
                NewsUtils.getIsFetchingForCategory(getContext(),
                        mCategory));

        showLatestUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
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
        mAdapter.changeCursor(data);

        if (data != null && data.getCount() != 0) {
            TextView noData = mRootView.findViewById(R.id.news_no_data);
            noData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);

        TextView noData = mRootView.findViewById(R.id.news_no_data);
        noData.setVisibility(View.VISIBLE);
    }

    private void refresh() {
        //Call the fetch news service
        Intent intent = new Intent(getContext(), FetchNewsService.class);
        intent.putExtra(Constants.IntentConstants.EXTRA_NEWS_CATEGORY, mCategory);
        getContext().startService(intent);
    }

    //Callback from FetchNewsService
    @SuppressLint("SwitchIntDef")
    private void onFetchFinished(final Context context, @BlichSyncTask.FetchStatus int status) {

        if (status == BlichSyncTask.FETCH_STATUS_SUCCESSFUL) {
            Snackbar.make(mRootView,
                    R.string.snackbar_fetch_successful,
                    Snackbar.LENGTH_LONG)
                    .show();
        } else {
            @SuppressLint("InflateParams")
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_fetch_failed,
                    null);

            @StringRes int titleString;
            @StringRes int messageString;
            switch (status) {
                case BlichSyncTask.FETCH_STATUS_NO_CONNECTION: {
                    titleString = R.string.dialog_fetch_no_connection_title;
                    messageString = R.string.dialog_fetch_no_connection_message;
                    break;
                }
                case BlichSyncTask.FETCH_STATUS_EMPTY_HTML: {
                    titleString = R.string.dialog_fetch_empty_html_title;
                    messageString = R.string.dialog_fetch_empty_html_message;
                    break;
                }
                default:
                    titleString = R.string.dialog_fetch_unsuccessful_title;
                    messageString = R.string.dialog_fetch_unsuccessful_message;
            }
            TextView title = dialogView.findViewById(R.id.dialog_title);
            title.setText(titleString);
            TextView message = dialogView.findViewById(R.id.dialog_message);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String isFetchingKey = getString(R.string.pref_is_fetching_news_key) + mCategory;
        if (key.equals(isFetchingKey)) {
            mSwipeRefreshLayout.setRefreshing(NewsUtils.getIsFetchingForCategory(getContext(), mCategory));
        }
    }

    private class StatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Callback
            @BlichSyncTask.FetchStatus int status =
                    intent.getIntExtra(Constants.IntentConstants.EXTRA_NEWS_CATEGORY,
                    BlichSyncTask.FETCH_STATUS_UNSUCCESSFUL);
            onFetchFinished(getContext(), status);
        }
    }

    private void showLatestUpdate() {
        long latestUpdateInMillis = NewsUtils.getLatestUpdateForCategory(
                getContext(),
                mCategory
        );

        if (latestUpdateInMillis == 0) {
            return;
        }

        String dateString = (String) DateUtils.getRelativeTimeSpanString(
                getContext(),
                latestUpdateInMillis
        );

        FrameLayout view = mRootView.findViewById(R.id.frame_layout);
        Snackbar snackbar = Snackbar.make(
                view,
                "עודכן לאחרונה ב -  " + dateString,
                Snackbar.LENGTH_INDEFINITE
        );

        try {
            Field mAccessibilityManagerField = BaseTransientBottomBar.class.getDeclaredField("mAccessibilityManager");
            mAccessibilityManagerField.setAccessible(true);
            AccessibilityManager accessibilityManager = (AccessibilityManager) mAccessibilityManagerField.get(snackbar);
            Field mIsEnabledField = AccessibilityManager.class.getDeclaredField("mIsEnabled");
            mIsEnabledField.setAccessible(true);
            mIsEnabledField.setBoolean(accessibilityManager, false);
            mAccessibilityManagerField.set(snackbar, accessibilityManager);
        } catch (NoSuchFieldException e) {
            Timber.e(e.getMessage());
        } catch (IllegalAccessException e) {
            Timber.e(e.getMessage());
        }

        snackbar.show();
    }
}

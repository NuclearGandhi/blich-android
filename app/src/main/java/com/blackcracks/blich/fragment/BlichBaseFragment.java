package com.blackcracks.blich.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Utilities;

/**
 * A base fragment for most of the fragments in the app.
 * Every fragment the extends this class, must have a {@link android.support.v7.widget.Toolbar}
 * and a {@link android.support.v4.widget.SwipeRefreshLayout}.
 */

public abstract class BlichBaseFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver mSyncBroadcastReceiver;

    public BlichBaseFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(getFragmentLayout(), container, false);

        mSyncBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @BlichSyncAdapter.FetchStatus int status =
                        intent.getIntExtra(BlichSyncAdapter.FETCH_STATUS,
                                BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL);
                Utilities.onSyncFinished(getContext(), mRootView, status);
            }
        };

        mSwipeRefreshLayout =
                (SwipeRefreshLayout) mRootView.findViewById(R.id.swiperefresh_schedule);
        mSwipeRefreshLayout.setEnabled(false);

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(getFragmentTitle());

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
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResource(), menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(BlichSyncAdapter.ACTION_SYNC_FINISHED));
        mSwipeRefreshLayout.setRefreshing(Utilities.getPreferenceBoolean(getContext(), getString(R.string.pref_is_fetching_key), false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mSyncBroadcastReceiver);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_is_fetching_key))) {
            mSwipeRefreshLayout.setRefreshing(sharedPreferences.getBoolean(getString(R.string.pref_is_fetching_key), true));
        }
    }

    protected abstract @LayoutRes int getFragmentLayout();

    protected abstract @StringRes int getFragmentTitle();

    protected abstract @MenuRes int getMenuResource();
}

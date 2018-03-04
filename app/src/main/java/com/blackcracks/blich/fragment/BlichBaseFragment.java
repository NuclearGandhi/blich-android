/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.sync.BlichSyncIntentService;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.SyncUtils;

/**
 * A base fragment for most of the fragments in the app.
 * Every fragment the extends this class, must have a {@link android.support.v7.widget.Toolbar}
 * and a {@link android.support.v4.widget.SwipeRefreshLayout}.
 */
@SuppressWarnings("ConstantConditions")
public abstract class BlichBaseFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private NavigationView mNavigationView;
    private DrawerArrowDrawable mHamburgerDrawable;

    private BroadcastReceiver mSyncBroadcastReceiver;

    public BlichBaseFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(getFragmentLayout(), container, false);

        mSwipeRefreshLayout =
                mRootView.findViewById(R.id.swiperefresh_schedule);
        mSwipeRefreshLayout.setEnabled(false);

        final SyncUtils.OnSyncRetryListener onSyncRetryListener = new SyncUtils.OnSyncRetryListener() {
            @Override
            public void onRetry() {
                SyncUtils.syncDatabase(getContext());
            }
        };

        //Create a BroadcastReceiver to listen when a sync has finished.
        mSyncBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                @SyncUtils.FetchStatus int status = intent.getIntExtra(
                        BlichSyncIntentService.EXTRA_FETCH_STATUS,
                        SyncUtils.FETCH_STATUS_UNSUCCESSFUL);

                SyncUtils.syncFinishedCallback(getActivity(), status, true, onSyncRetryListener);
            }
        };

        return mRootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(getFragmentTitle());

        DrawerLayout drawerLayout = activity.getDrawerLayout();

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar, R.string.drawer_open_desc, R.string.drawer_close_desc) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        mHamburgerDrawable = drawerToggle.getDrawerArrowDrawable();
        mNavigationView = drawerLayout.findViewById(R.id.nav_view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResource(), menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateATE();

        boolean isRefreshing = PreferencesUtils.getBoolean(getContext(), Preferences.PREF_IS_SYNCING_KEY);
        mSwipeRefreshLayout.setRefreshing(
                PreferencesUtils.getBoolean(getContext(), Preferences.PREF_IS_SYNCING_KEY));
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mSyncBroadcastReceiver,
                        new IntentFilter(BlichSyncIntentService.ACTION_SYNC_FINISHED_CALLBACK));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mSyncBroadcastReceiver);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.getKey(getContext(), Preferences.PREF_IS_SYNCING_KEY))) {
            mSwipeRefreshLayout.setRefreshing(PreferencesUtils.getBoolean(getContext(), Preferences.PREF_IS_SYNCING_KEY));
        }
    }

    @CallSuper
    protected void invalidateATE() {
        String ateKey = ((MainActivity) getActivity()).getATEKey();
        mHamburgerDrawable.setColor(Config.getToolbarTitleColor(getContext(), null, ateKey));

        ImageView view = mNavigationView.getHeaderView(0).findViewById(R.id.header_image);
        int toolbarColor = Config.toolbarColor(getContext(), ateKey, null);
        boolean isToolbarLight = Config.isLightToolbar(getContext(), null, ateKey, toolbarColor);
        if (isToolbarLight) {
            view.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.logo_grey));
        } else {
            view.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.logo_white));
        }
    }

    protected abstract @LayoutRes
    int getFragmentLayout();

    protected abstract @StringRes
    int getFragmentTitle();

    protected abstract @MenuRes
    int getMenuResource();
}

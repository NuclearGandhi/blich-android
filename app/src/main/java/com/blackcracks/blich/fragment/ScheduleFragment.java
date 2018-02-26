/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.fragment;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.PreferencesUtils;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;

/**
 * The {@link ScheduleFragment} class is responsible for getting and displaying the desired schedule
 * for the user.
 */
@SuppressWarnings("ConstantConditions")
public class ScheduleFragment extends BlichBaseFragment {

    private View mRootView;
    private ImageButton mFilterActionButton;

    Toolbar mToolbar;
    TabLayout mTabLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        mToolbar = mRootView.findViewById(R.id.toolbar);
        mTabLayout = mRootView.findViewById(R.id.tablayout_schedule_days);

        ViewPager viewPager = mRootView.findViewById(R.id.viewpager_schedule);
        SchedulePagerAdapter pagerAdapter = new SchedulePagerAdapter(
                getChildFragmentManager(),
                getResources().getStringArray(R.array.tab_schedule_names));
        if (viewPager != null) {
            viewPager.setAdapter(pagerAdapter);

            int day = ScheduleUtils.getWantedDayOfTheWeek();
            viewPager.setCurrentItem(SchedulePagerAdapter.getRealPosition(day - 1), false);

        }
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(viewPager);
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        return mRootView;
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_schedule;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.drawer_schedule_title;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_schedule;
    }

    @Override
    protected void invalidateATE() {
        super.invalidateATE();
        String ateKey = ((MainActivity) getActivity()).getATEKey();
        ATE.themeView(mToolbar, ateKey);
        ATE.themeView(mTabLayout, ateKey);

        if (mFilterActionButton != null) tintFilterAction(ateKey);

        int toolbarColor = Config.toolbarColor(getContext(), ateKey, mToolbar);
        boolean isToolbarLight = Config.isLightToolbar(getContext(), mToolbar, ateKey, toolbarColor);
        if (isToolbarLight) {
            mTabLayout.setTabTextColors(ContextCompat.getColorStateList(getContext(), R.color.text_color_light));
            mTabLayout.setSelectedTabIndicatorColor(Config.accentColor(getContext(), ateKey));
        } else {
            mTabLayout.setTabTextColors(ContextCompat.getColorStateList(getContext(), R.color.text_color_dark));
            mTabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //Get the menu item
        final MenuItem filter = menu.findItem(R.id.action_filter_toggle);
        //Inflate it with a view
        mFilterActionButton = (ImageButton) LayoutInflater.from(getContext())
                .inflate(R.layout.menu_filter_list, null, false);
        filter.setActionView(mFilterActionButton);

        tintFilterAction(((MainActivity) getActivity()).getATEKey());

        //Get the filter toggle state
        boolean isFilterOn = PreferencesUtils.getBoolean(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);

        //Set the correct image according to the filter toggle state
        if (!isFilterOn) {
            mFilterActionButton.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_disabled_filter_list_white_24dp)
            );
        }

        mFilterActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilterAction();
            }
        });

        //Set appropriate tooltips, according to api version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFilterActionButton.setTooltipText(getString(R.string.action_button_filter_toggle));
        } else {
            mFilterActionButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    int x = v.getLeft();
                    int y = v.getTop() + 2 * v.getHeight();
                    Toast toast = Toast.makeText(getContext(), R.string.action_button_filter_toggle, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.END, x, y);
                    toast.show();
                    return true;
                }
            });
        }
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
    public void onResume() {
        super.onResume();
        //Get the filter toggle state
        boolean isFilterOn = PreferencesUtils.getBoolean(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);
        //Set the correct image according to the filter toggle state
        if (mFilterActionButton != null) {
            int drawableId;
            if (isFilterOn) drawableId = R.drawable.ic_filter_list_white_24dp;
            else drawableId = R.drawable.ic_disabled_filter_list_white_24dp;
            mFilterActionButton.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), drawableId)
            );
        }
    }

    private void toggleFilterAction() {
        //Get the filter toggle state
        String prefKey = Preferences.getKey(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);
        boolean isFilterOn = PreferencesUtils.getBoolean(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);

        //Test if the user had setup filtering in the settings
        String filterSelect = PreferencesUtils.getString(getContext(), Preferences.PREF_FILTER_SELECT_KEY);
        if (filterSelect.equals("") && !isFilterOn) {
            Toast.makeText(getContext(), R.string.toast_filter_not_setup, Toast.LENGTH_LONG)
                    .show();

            return;
        }

        //Reverse the filter toggle state
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(prefKey, !isFilterOn)
                .apply();

        //Get the action view
        if (isFilterOn) { //We need to disable it
            //If API > 21, start an animation, else simply change the image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startFilterActionAnimation(false);
            } else {
                //Change image
                mFilterActionButton.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_disabled_filter_list_white_24dp)
                );
            }
        } else {
            //If API > 21, start an animation, else simply change the image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startFilterActionAnimation(true);
            } else {
                //Change image
                mFilterActionButton.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_list_white_24dp));
            }
        }
    }

    private void tintFilterAction(String ateKey) {
        mFilterActionButton.setImageTintList(ColorStateList.valueOf(Config.getToolbarTitleColor(getContext(), mToolbar, ateKey)));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startFilterActionAnimation(final boolean enable) {
        //Get the drawable id
        int drawableId;
        if (enable) drawableId = R.drawable.anim_enable_filter_man;
        else drawableId = R.drawable.anim_disable_filter;

        AnimatedVectorDrawable animated = (AnimatedVectorDrawable)
                ContextCompat.getDrawable(getContext(), drawableId);

        //Begin the animation
        mFilterActionButton.setImageDrawable(animated);
        animated.start();

        //Disable the button while animating
        Handler handler = new Handler();
        mFilterActionButton.setEnabled(false);
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        //Change image at the end of the animation
                        int imageId;
                        if (enable) imageId = R.drawable.ic_filter_list_white_24dp;
                        else imageId = R.drawable.ic_disabled_filter_list_white_24dp;
                        mFilterActionButton.setImageDrawable(
                                ContextCompat.getDrawable(getContext(), imageId));
                        mFilterActionButton.setEnabled(true);
                    }
                },
                500);
    }
}

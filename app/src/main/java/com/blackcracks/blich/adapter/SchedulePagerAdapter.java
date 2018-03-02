/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackcracks.blich.fragment.ScheduleDayFragment;

/**
 * A {@link FragmentStatePagerAdapter} containing 6 tabs each representing the school days of the week.
 * It handles right to left tab layout.
 */
public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final int TABS_COUNT = 6;

    private final String[] mTabNames;

    /**
     * @param tabNames An array representing the tab titles.
     */
    public SchedulePagerAdapter(FragmentManager fm,
                                String[] tabNames) {
        super(fm);
        mTabNames = tabNames;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(ScheduleDayFragment.DAY_KEY, getRealPosition(position) + 1);
        ScheduleDayFragment fragment = new ScheduleDayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabNames[getRealPosition(position)];
    }

    /**
     * Get the "real" position, from the old position
     * The adapter is set to left to right, but acts like a right to left,
     * making the positions inverted. This method is used to get the "real" position of a page.
     *
     * @param position old position to convert to the "real" position
     * @return the "real" position.
     */
    public static int getRealPosition(int position) {
        return TABS_COUNT - 1 - position;
    }
}

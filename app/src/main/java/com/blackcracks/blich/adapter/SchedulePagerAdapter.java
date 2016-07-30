package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackcracks.blich.fragment.ScheduleDayFragment;

public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final int TABS_COUNT = 6;

    private String[] mTabNames;

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

    public static int getRealPosition(int position) {
        return 5 - position;
    }
}

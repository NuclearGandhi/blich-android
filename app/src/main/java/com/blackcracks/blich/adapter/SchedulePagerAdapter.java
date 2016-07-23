package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blackcracks.blich.fragment.ScheduleDayFragment;

public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final int TABS_COUNT = 6;

    private String[] mTabNames;
    private ScheduleDayFragment.OnScrollListener mOnScrollListener;

    public SchedulePagerAdapter(FragmentManager fm,
                                String[] tabNames,
                                @Nullable ScheduleDayFragment.OnScrollListener listener) {
        super(fm);
        mTabNames = tabNames;
        mOnScrollListener = listener;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(ScheduleDayFragment.DAY_KEY, getRealPosition(position) + 1);
        ScheduleDayFragment fragment = new ScheduleDayFragment();
        fragment.setArguments(args);
        if (mOnScrollListener != null) {
            fragment.addOnScrollListener(mOnScrollListener);
        }
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

    public int getRealPosition(int position) {
        return 5 - position;
    }
}

package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.blackcracks.blich.fragment.ScheduleDayFragment;

public class SchedulePagerAdapter extends FragmentStatePagerAdapter {

    private static final int TABS_COUNT = 6;

    private final String[] mTabNames;

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

    //The adapter is set to left to right, but acts like a right to left, making the
    //positions inverted. This method is used to get the "real" position of a page.
    public static int getRealPosition(int position) {
        return TABS_COUNT - 1 - position;
    }

    public static class OnDayChangeListener implements ViewPager.OnPageChangeListener {

        int currentPosition = 0;
        SchedulePagerAdapter mAdapter;

        public OnDayChangeListener(SchedulePagerAdapter pagerAdapter) {
            mAdapter = pagerAdapter;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int newPosition) {
            FragmentLifecycle fragmentToShow = (FragmentLifecycle) mAdapter.getItem(newPosition);
            fragmentToShow.onResumeFragment();

            FragmentLifecycle fragmentToHide = (FragmentLifecycle) mAdapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = newPosition;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public interface FragmentLifecycle {

        public void onPauseFragment();
        public void onResumeFragment();

    }
}

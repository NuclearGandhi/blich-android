package com.blackcracks.blich.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class NewsPagerAdapter extends FragmentPagerAdapter {

    private String[] mTabNames;

    public NewsPagerAdapter(FragmentManager fm, String[] tabNames) {
        super(fm);
        mTabNames = tabNames;
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return mTabNames.length;
    }
}

package com.blackcracks.blich.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.blackcracks.blich.fragment.NewsCategoryFragment;

public class NewsPagerAdapter extends FragmentPagerAdapter {

    private String[] mTabNames;

    public NewsPagerAdapter(FragmentManager fm, String[] tabNames) {
        super(fm);
        mTabNames = tabNames;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(NewsCategoryFragment.KEY_CATEGORY, position);

        Fragment fragment = new NewsCategoryFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return mTabNames.length;
    }
}

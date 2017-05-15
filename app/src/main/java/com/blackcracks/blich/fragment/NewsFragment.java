package com.blackcracks.blich.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.NewsPagerAdapter;

public class NewsFragment extends Fragment {

    View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        TabLayout tabLayout = (TabLayout) mRootView.findViewById(R.id.tablayout);
        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(
                new NewsPagerAdapter(getFragmentManager(),
                        getContext().getResources().getStringArray(R.array.tab_news_names)));


        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        return mRootView;
    }
}

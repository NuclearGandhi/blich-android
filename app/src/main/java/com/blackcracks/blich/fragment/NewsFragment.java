package com.blackcracks.blich.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.NewsPagerAdapter;
import com.blackcracks.blich.data.FetchNewsService;
import com.blackcracks.blich.util.Constants;

public class NewsFragment extends BlichBaseFragment {

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

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_news;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.drawer_news_title;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_news;
    }

    @Override
    protected String getFetchCallback() {
        return Constants.IntentConstants.ACTION_FETCH_NEWS_CALLBACK;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                Intent intent = new Intent(getContext(), FetchNewsService.class);
                getContext().startService(intent);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}

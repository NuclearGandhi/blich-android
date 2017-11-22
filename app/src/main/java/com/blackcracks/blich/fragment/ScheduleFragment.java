package com.blackcracks.blich.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.util.Utilities;

/**
 * The ScheduleFragment class is responsible for getting and displaying the schedule
 * of the user
 */
public class ScheduleFragment extends BlichBaseFragment {

    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        TabLayout tabLayout = mRootView.findViewById(R.id.tablayout_schedule_days);
        ViewPager viewPager = mRootView.findViewById(R.id.viewpager_schedule);
        SchedulePagerAdapter pagerAdapter = new SchedulePagerAdapter(
                getChildFragmentManager(),
                getResources().getStringArray(R.array.tab_schedule_names));

        if (viewPager != null) {
            viewPager.setAdapter(pagerAdapter);

            int day = Utilities.Schedule.getWantedDayOfTheWeek();
            viewPager.setCurrentItem(SchedulePagerAdapter.getRealPosition(day - 1), false);

        }
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
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
}

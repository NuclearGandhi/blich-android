package com.blackcracks.blich.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;

/**
 * The ScheduleFragment class is responsible for getting and displaying the schedule
 * of the user
 */
public class ScheduleFragment extends BlichBaseFragment {

    private static final String LOG_TAG = ScheduleFragment.class.getSimpleName();

    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        TabLayout tabLayout = (TabLayout) mRootView.findViewById(R.id.tablayout_schedule_days);
        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.viewpager_schedule);
        if (viewPager != null) {
            viewPager.setAdapter(
                    new SchedulePagerAdapter(getActivity().getSupportFragmentManager(),
                            getResources().getStringArray(R.array.tab_schedule_names)));

            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour > 18) day = day % 7 + 1;
            switch (day) {
                case 1: {
                    day = 0;
                    break;
                }
                case 2: {
                    day = 1;
                    break;
                }
                case 3: {
                    day = 2;
                    break;
                }
                case 4: {
                    day = 3;
                    break;
                }
                case 5: {
                    day = 4;
                    break;
                }
                case 6: {
                    day = 5;
                    break;
                }
                case 7: {
                    day = 0;
                    break;
                }
            }
            viewPager.setCurrentItem(SchedulePagerAdapter.getRealPosition(day), false);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_collapse_expand).setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                Utilities.updateBlichData(getContext(), mRootView);
                return true;
            }
            case R.id.action_collapse_expand: {
                boolean isCollapsed = Utilities.getPreferenceBoolean(getContext(),
                        getString(R.string.pref_is_collapsed_key),
                        getContext().getResources().getBoolean(R.bool.pref_is_collapsed_default_value));

                if (isCollapsed) item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_collapse));
                else item.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand));

                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putBoolean(getString(R.string.pref_is_collapsed_key), !isCollapsed)
                        .apply();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

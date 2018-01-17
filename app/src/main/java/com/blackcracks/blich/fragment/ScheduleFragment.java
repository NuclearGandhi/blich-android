package com.blackcracks.blich.fragment;


import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.SchedulePagerAdapter;
import com.blackcracks.blich.util.Constants.Preferences;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem filter = menu.findItem(R.id.action_filter_toggle);
        ImageView icon = (ImageView) LayoutInflater.from(getContext())
                .inflate(R.layout.menu_filter_list, null, false);
        filter.setActionView(icon);
        boolean isFilterOn = Utilities.getPrefBoolean(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilterAction(filter);
            }
        });

        icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(), R.string.action_button_filter_toggle, Toast.LENGTH_SHORT)
                        .show();
                return true;
            }
        });

        if (!isFilterOn) {
            icon.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_disabled_filter_list_white_24dp)
            );
        }
    }

    private void toggleFilterAction(MenuItem item) {
        String prefKey = Preferences.getKey(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);
        boolean isFilterOn = Utilities.getPrefBoolean(getContext(), Preferences.PREF_FILTER_TOGGLE_KEY);
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putBoolean(prefKey, !isFilterOn)
                .apply();

        final ImageView icon = (ImageView) item.getActionView();
        if (isFilterOn) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                //Begin animation
                AnimatedVectorDrawable animated = (AnimatedVectorDrawable)
                        ContextCompat.getDrawable(getContext(), R.drawable.anim_ant_man);

                icon.setImageDrawable(animated);

                Handler handler = new Handler();
                animated.start();
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                //Change image
                                icon.setImageDrawable(
                                        ContextCompat.getDrawable(getContext(), R.drawable.ic_disabled_filter_list_white_24dp));
                            }
                        },
                        1539);
            } else {
                //Change image
                icon.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_disabled_filter_list_white_24dp)
                );
            }
        } else {
            //Change image
            icon.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_filter_list_white_24dp));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                Utilities.updateBlichData(getContext(), mRootView);
                return true;
            }
            case R.id.action_filter_toggle: {
                toggleFilterAction(item);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

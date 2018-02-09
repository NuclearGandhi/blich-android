package com.blackcracks.blich.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.listener.AppBarStateChangeListener;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.Sort;

public class ExamsFragment extends BlichBaseFragment implements View.OnClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<Exam>>{

    private static final int EXAM_LOADER_ID = 1;

    private Realm mRealm;
    private RealmChangeListener<Realm> mChangeListener;

    private View mRootView;

    private AppBarLayout mAppBarLayout;
    private ImageView mDropDown;
    private ListView mListView;

    private ExamAdapter mAdapter;

    private final List<CalendarDay> mDates = new ArrayList<>();

    private boolean mIsExpanded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        setUpRefresher();

        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        mAppBarLayout = mRootView.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, @State int state) {
                if (state == AppBarStateChangeListener.COLLAPSED) {
                    ViewCompat.animate(mDropDown).rotation(0).start();
                    mIsExpanded = false;
                } else if (state == AppBarStateChangeListener.EXPANDED) {
                    ViewCompat.animate(mDropDown).rotation(180).start();
                    mIsExpanded = true;
                }
            }
        });

        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);

        mDropDown = mRootView.findViewById(R.id.drop_down_arrow);
        toolbar.setOnClickListener(this);

        MaterialCalendarView calendarView = mRootView.findViewById(R.id.calendar_view);
        calendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        calendarView.setCurrentDate(new Date());
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    for (int i = 0; i < mDates.size(); i++) {
                        CalendarDay event = mDates.get(i);
                        if (event.equals(date)) {
                            ViewCompat.animate(mDropDown).rotation(0).start();
                            mAppBarLayout.setExpanded(false, true);
                            mIsExpanded = false;
                            mListView.smoothScrollToPosition(i);
                            mListView.setSelection(i);
                        }
                    }
                }
            }
        });

        mListView = mRootView.findViewById(R.id.list_view_exam);
        mAdapter = new ExamAdapter(getContext(), null);
        mListView.setAdapter(mAdapter);

        ViewCompat.setNestedScrollingEnabled(mListView, true);

        return mRootView;
    }


    private void setUpRefresher() {
        mChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                if (isAdded()) {
                    getLoaderManager().restartLoader(
                            EXAM_LOADER_ID,
                            Bundle.EMPTY,
                            ExamsFragment.this);
                }
            }
        };
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(Constants.EXAMS_LOADER_ID,
                null,
                this);
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

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_exams;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.drawer_exams_title;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_exams;
    }

    @Override
    public void onClick(View v) {
        if (mIsExpanded) {
            ViewCompat.animate(mDropDown).rotation(0).start();
            mAppBarLayout.setExpanded(false, true);
            mIsExpanded = false;
        } else {
            ViewCompat.animate(mDropDown).rotation(180).start();
            mAppBarLayout.setExpanded(true, true);
            mIsExpanded = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRealm.addChangeListener(mChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(mChangeListener);
        mRealm.close();
    }

    @Override
    public Loader<List<Exam>> onCreateLoader(int id, Bundle args) {
        return new ExamsLoader(getContext(), mRealm);
    }

    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> data) {
        mAdapter.switchData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Exam>> loader) {
        mAdapter.switchData(null);
    }

    private static class ExamsLoader extends Loader<List<Exam>> {

        private Realm mRealm;

        public ExamsLoader(@NonNull Context context, Realm realm) {
            super(context);
            mRealm = realm;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            List<Exam> exams = mRealm.where(Exam.class)
                    .sort("date", Sort.ASCENDING)
                    .findAll();

            deliverResult(exams);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }
}

/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.GenericExam;
import com.blackcracks.blich.listener.AppBarStateChangeListener;
import com.blackcracks.blich.util.ExamUtils;
import com.blackcracks.blich.util.SyncUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.Sort;
import timber.log.Timber;

/**
 * A {@link android.support.v4.app.Fragment} containing a calendar and a list of upcoming exams.
 */
@SuppressWarnings("ConstantConditions")
public class ExamsFragment extends BlichBaseFragment implements View.OnClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<Exam>>{

    private static final int EXAM_LOADER_ID = 1;

    private Realm mRealm;
    private RealmChangeListener<Realm> mChangeListener;

    private View mRootView;

    private AppBarLayout mAppBarLayout;
    private ImageView mDropDown;
    private MaterialCalendarView mCalendarView;
    private ListView mListView;
    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingTb;

    private ExamAdapter mAdapter;

    private final List<CalendarDay> mDates = new ArrayList<>();

    private boolean mIsExpanded = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        setUpRefresher();

        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        mToolbar = mRootView.findViewById(R.id.toolbar);
        mToolbar.setOnClickListener(this);
        mCollapsingTb = mRootView.findViewById(R.id.collapsingToolbar);

        mDropDown = mRootView.findViewById(R.id.drop_down_arrow);

        mCalendarView = mRootView.findViewById(R.id.calendar_view);
        mCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        mCalendarView.setTopbarVisible(false);
        mCalendarView.setCurrentDate(new Date());

        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
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

        final String fragmentName = getString(R.string.drawer_exams_title);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM - yyyy", new Locale("iw"));

        mCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                updateTitle(dateFormat, mToolbar);
            }
        });

        mAppBarLayout = mRootView.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(@State int state) {
                if (state == AppBarStateChangeListener.COLLAPSED) {
                    mToolbar.setTitle(fragmentName);

                    ViewCompat.animate(mDropDown).rotation(0).start();
                    mIsExpanded = false;
                } else if (state == AppBarStateChangeListener.EXPANDED) {
                    updateTitle(dateFormat, mToolbar);

                    ViewCompat.animate(mDropDown).rotation(180).start();
                    mIsExpanded = true;
                }
            }
        });

        mListView = mRootView.findViewById(R.id.list_view_exam);
        TextView statusMessage = mRootView.findViewById(R.id.exam_no_data_status);
        mAdapter = new ExamAdapter(getContext(), null, statusMessage);
        mListView.setAdapter(mAdapter);

        ViewCompat.setNestedScrollingEnabled(mListView, true);

        return mRootView;
    }

    private void setUpRefresher() {
        mChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(@NonNull Realm realm) {
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
        getLoaderManager().restartLoader(
                EXAM_LOADER_ID,
                null,
                this
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                SyncUtils.syncDatabase(getContext());
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
    protected void invalidateATE() {
        super.invalidateATE();
        String ateKey = ((MainActivity) getActivity()).getATEKey();

        ATE.themeView(mToolbar, ateKey);
        mCollapsingTb.setExpandedTitleColor(Config.getToolbarTitleColor(getContext(), mToolbar, ateKey));
        ATE.themeView(mCalendarView, ateKey);

        Drawable drawable = mDropDown.getDrawable();
        drawable.setTint(Config.getToolbarTitleColor(
                getContext(),
                mToolbar,
                ateKey));

        int toolbarColor = Config.toolbarColor(getContext(), ateKey, mToolbar);
        boolean isToolbarLight = Config.isLightToolbar(getContext(), mToolbar, ateKey, toolbarColor);
        if (isToolbarLight) {
            mCalendarView.setDateTextAppearance(R.style.TextAppearance_Toolbar_Light);
            mCalendarView.setWeekDayTextAppearance(R.style.TextAppearance_Toolbar_Light);
        } else {
            mCalendarView.setDateTextAppearance(R.style.TextAppearance_Toolbar_Dark);
            mCalendarView.setWeekDayTextAppearance(R.style.TextAppearance_Toolbar_Dark);
        }
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

        try {
            if (!data.isEmpty()) {
                loadDataIntCalendar(data);
            }
        } catch (IllegalStateException e) {
            Timber.d("Realm instance has been closed");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Exam>> loader) {
        mAdapter.switchData(null);
    }

    private void loadDataIntCalendar(List<Exam> data) {
        List<GenericExam> exams = ExamUtils.buildExamsList(data);
        for (GenericExam exam:
                exams) {
            mDates.add(CalendarDay.from(exam.getDate()));
        }

        CalendarDay minDate = mDates.get(0);
        CalendarDay maxDate = mDates.get(mDates.size() - 1);
        mCalendarView.state().edit()
                .setMinimumDate(minDate)
                .setMaximumDate(maxDate)
                .commit();

        DayViewDecorator decorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return mDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new DotSpan(5, Color.WHITE));
            }
        };

        mCalendarView.addDecorator(decorator);
    }

    private void updateTitle(DateFormat dateFormat, Toolbar toolbar) {
        toolbar.setTitle(dateFormat.format(mCalendarView.getCurrentDate().getDate()));
    }

    /**
     * A {@link Loader} to fetch {@link List<Exam>} from {@link Realm} database.
     */
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

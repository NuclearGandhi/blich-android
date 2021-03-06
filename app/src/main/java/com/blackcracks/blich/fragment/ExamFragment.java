/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.exam.Exam;
import com.blackcracks.blich.widget.listener.AppBarStateChangeListener;
import com.blackcracks.blich.util.SyncCallbackUtils;
import com.blackcracks.blich.util.Utilities;
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
public class ExamFragment extends BlichBaseFragment implements
        View.OnClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<Exam>>{

    private static final int EXAM_LOADER_ID = 1;

    private Realm mRealm;
    private RealmChangeListener<Realm> mChangeListener;

    private AppBarLayout mAppBarLayout;
    private ImageView mDropDown;
    private MaterialCalendarView mCalendarView;
    private RecyclerView mRecyclerView;
    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingTb;

    private View mDataStatusView;
    private ExamAdapter mAdapter;

    private final List<CalendarDay> mDates = new ArrayList<>();

    private boolean mIsExpanded = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        setUpRefresher();

        final View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mToolbar = rootView.findViewById(R.id.toolbar);
        mToolbar.setOnClickListener(this);
        mCollapsingTb = rootView.findViewById(R.id.collapsingToolbar);

        mDropDown = rootView.findViewById(R.id.drop_down_arrow);

        mCalendarView = rootView.findViewById(R.id.calendar_view);
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
                    ViewCompat.animate(mDropDown).rotation(0).start();
                    mAppBarLayout.setExpanded(false, true);
                    scrollToDate(date);
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

        mAppBarLayout = rootView.findViewById(R.id.app_bar_layout);
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

        mDataStatusView = rootView.findViewById(R.id.message_no_data);

        mRecyclerView = rootView.findViewById(R.id.recycler_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ExamAdapter(
                getContext(),
                null);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setNestedScrollingEnabled(true);
        return rootView;
    }

    private void setUpRefresher() {
        mChangeListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(@NonNull Realm realm) {
                if (isAdded()) {
                    getLoaderManager().restartLoader(
                            EXAM_LOADER_ID,
                            Bundle.EMPTY,
                            ExamFragment.this);
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
                SyncCallbackUtils.syncDatabase(getContext());
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_exam;
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
            mCalendarView.setSelectionColor(ContextCompat.getColor(getContext(), R.color.button_pressed_light));
            mCalendarView.setDateTextAppearance(R.style.TextAppearance_Toolbar_Light);
            mCalendarView.setWeekDayTextAppearance(R.style.TextAppearance_Toolbar_Light);
        } else {
            mCalendarView.setSelectionColor(ContextCompat.getColor(getContext(), R.color.button_pressed_dark));
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

    private void loadDataIntoCalendar(List<Exam> data) {
        for (Exam exam :
                data) {
            mDates.add(CalendarDay.from(exam.getDate()));
        }

        CalendarDay maxDate = mDates.get(mDates.size() - 1);
        mCalendarView.state().edit()
                .setMinimumDate(new Date())
                .setMaximumDate(maxDate)
                .commit();

        String ateKey = Utilities.getATEKey(getContext());
        final int dotColor = ATEUtil.isColorLight(Config.primaryColor(getContext(), ateKey)) ?
                ContextCompat.getColor(getContext(), R.color.ate_primary_text_light) :
                ContextCompat.getColor(getContext(), R.color.ate_primary_text_dark);

        DayViewDecorator decorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return mDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new DotSpan(
                        5,
                        dotColor));
            }
        };

        mCalendarView.addDecorator(decorator);
    }

    private void updateTitle(DateFormat dateFormat, Toolbar toolbar) {
        toolbar.setTitle(dateFormat.format(mCalendarView.getCurrentDate().getDate()));
    }

    private void scrollToDate(CalendarDay date) {
        int position = mAdapter.getDateFlatPosition(date);
        if (position != -1)
            mRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public Loader<List<Exam>> onCreateLoader(int id, Bundle args) {
        return new ExamsLoader(getContext(), mRealm);
    }

    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> data) {
        mAdapter.setData(data);
        if (data.isEmpty())
            mDataStatusView.setVisibility(View.VISIBLE);
        else
            mDataStatusView.setVisibility(View.GONE);

        try {
            if (!data.isEmpty()) {
                loadDataIntoCalendar(data);
            }
        } catch (IllegalStateException e) {
            Timber.d("Realm instance has been closed");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Exam>> loader) {
        mAdapter.setData(null);
    }

    /**
     * A {@link Loader} to fetch {@link List< Exam >} from {@link Realm} database.
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

            List<Exam> rawExams = mRealm.where(Exam.class)
                    .sort("date", Sort.ASCENDING)
                    .findAll();

            deliverResult(rawExams);
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }
}

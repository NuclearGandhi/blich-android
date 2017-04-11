package com.blackcracks.blich.fragment;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.ExamAdapter;
import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.listener.AppBarStateChangeListener;
import com.blackcracks.blich.util.Utilities;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExamsFragment extends BlichBaseFragment implements View.OnClickListener{

    private static final String LOG_TAG = ExamsFragment.class.getSimpleName();

    private static final int EXAMS_LOADER_ID = 0;
    private static final int EVENTS_LOADER_ID = 1;

    private static final String[] EXAMS_COLUMNS = {
            ExamsEntry._ID,
            ExamsEntry.COL_DATE,
            ExamsEntry.COL_SUBJECT,
            ExamsEntry.COL_TEACHER
    };

    Context mContext;

    private View mRootView;
    private AppBarLayout mAppBarLayout;
    private ImageView mDropDown;
    private MaterialCalendarView mCalendarView;
    private ListView mListView;
    private ExamAdapter mAdapter;

    protected final List<CalendarDay> mDates = new ArrayList<>();

    private boolean mIsExpanded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);
        mContext = getContext();

        mAppBarLayout = (AppBarLayout) mRootView.findViewById(R.id.app_bar_layout);
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

        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        mDropDown = (ImageView) mRootView.findViewById(R.id.drop_down_arrow);
        toolbar.setOnClickListener(this);

        mCalendarView = (MaterialCalendarView) mRootView.findViewById(R.id.calendar_view);
        mCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
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

        mListView = (ListView) mRootView.findViewById(R.id.list_view_exam);
        mAdapter = new ExamAdapter(mContext, null);
        mListView.setAdapter(mAdapter);

        ViewCompat.setNestedScrollingEnabled(mListView, true);

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(EXAMS_LOADER_ID, null, new ExamsLoader());
        getLoaderManager().initLoader(EVENTS_LOADER_ID, null, new EventsLoader());
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

    private class ExamsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = BlichContract.ExamsEntry.CONTENT_URI;

            return new CursorLoader(
                    mContext,
                    uri,
                    EXAMS_COLUMNS,
                    null, null, null);
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            mAdapter.swapCursor(null);
        }
    }

    private class EventsLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri uri = BlichContract.ExamsEntry.CONTENT_URI;

            return new CursorLoader(
                    mContext,
                    uri,
                    EXAMS_COLUMNS,
                    null, null, null);
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            new LoadDataToCalendar().execute(data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            mAdapter.swapCursor(null);
        }
    }

    private class LoadDataToCalendar extends AsyncTask<Cursor, Void, Date[]> {

        @Override
        protected Date[] doInBackground(@NonNull Cursor... params) {
            if (mDates.size() != 0) mDates.clear();
            Cursor data = params[0];
            data.moveToFirst();
            for (int i = 0; i < data.getCount(); i++) {
                String teacher = data.getString(data.getColumnIndex(ExamsEntry.COL_TEACHER));
                if (!teacher.equals("wut")) {
                    String date = data.getString(data.getColumnIndex(ExamsEntry.COL_DATE));
                    long timeInMillis = Utilities.getTimeInMillisFromDate(date);
                    Calendar exam = Calendar.getInstance();
                    exam.setTimeInMillis(timeInMillis);
                    mDates.add(CalendarDay.from(exam));
                }
                data.moveToNext();
            }
            Date minDate = new Date();
            Date maxDate = minDate;
            if (data.moveToPosition(1)) {
                Calendar calendar = Calendar.getInstance();

                String date = data.getString(data.getColumnIndex(ExamsEntry.COL_DATE));
                calendar.setTimeInMillis(Utilities.getTimeInMillisFromDate(date));
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                minDate = calendar.getTime();

                data.moveToLast();
                date = data.getString(data.getColumnIndex(ExamsEntry.COL_DATE));
                calendar.setTimeInMillis(Utilities.getTimeInMillisFromDate(date));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                maxDate = calendar.getTime();
            }
            Log.d(LOG_TAG, "min date = " + minDate + " max date = " + maxDate);
            return new Date[]{minDate, maxDate};
        }

        @Override
        protected void onPostExecute(Date[] dates) {
            Date minDate = dates[0];
            Date maxDate = dates[1];
            mCalendarView.state().edit()
                    .setMinimumDate(minDate)
                    .setMaximumDate(maxDate)
                    .commit();

            mCalendarView.addDecorator(new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return mDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.addSpan(new DotSpan(4, Color.WHITE));
                }
            });
        }
    }
}

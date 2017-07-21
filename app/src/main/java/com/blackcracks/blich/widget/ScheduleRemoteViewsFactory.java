package com.blackcracks.blich.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.LessonEntry;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;

public class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;

    private Cursor mLessonCursor;
    private Cursor mScheduleCursor;

    private int mDayOfTheWeek;

    public ScheduleRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(Constants.Widget.EXTRA_WIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        mDayOfTheWeek = Utilities.Schedule.getWantedDayOfTheWeek();
    }

    @Override
    public void onDataSetChanged() {

        if (mScheduleCursor != null) {
            mScheduleCursor.close();
        }

        callScheduleCursor();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mScheduleCursor == null) {
            return 0;
        } else {
            return mScheduleCursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {

        callLessonCursor(position + 1); //position = hour - 1

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_item);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(position + 1)); //position = hour - 1

        views.removeAllViews(R.id.widget_schedule_group);

        for (int i = 0; i < mLessonCursor.getCount(); i++) {
            mLessonCursor.moveToPosition(i);
            String subject = mLessonCursor.getString(mLessonCursor.getColumnIndex(LessonEntry.COL_SUBJECT));
            String lessonType = mLessonCursor.getString(mLessonCursor.getColumnIndex(LessonEntry.COL_LESSON_TYPE));

            int textColor;
            switch (lessonType) {
                case LessonEntry.LESSON_TYPE_CANCELED: {
                    textColor = ContextCompat.getColor(mContext, R.color.lesson_canceled);
                    break;
                }
                case LessonEntry.LESSON_TYPE_CHANGED: {
                    textColor = ContextCompat.getColor(mContext, R.color.lesson_changed);
                    break;
                }
                case LessonEntry.LESSON_TYPE_EXAM: {
                    textColor = ContextCompat.getColor(mContext, R.color.lesson_exam);
                    break;
                }
                case LessonEntry.LESSON_TYPE_EVENT: {
                    textColor = ContextCompat.getColor(mContext, R.color.lesson_event);
                    break;
                }
                default: {
                    textColor = ContextCompat.getColor(mContext, R.color.black_text);
                    break;
                }
            }

            RemoteViews info = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_info);
            info.setTextViewText(R.id.widget_schedule_subject, subject);
            info.setTextColor(R.id.widget_schedule_subject, textColor);

            views.addView(R.id.widget_schedule_group, info);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mScheduleCursor == null) return 0;
        mScheduleCursor.moveToPosition(position);
        return (long) mScheduleCursor.getInt(mScheduleCursor.getColumnIndex(ScheduleEntry._ID));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void callLessonCursor(int hour) {

        if (mLessonCursor != null) {
            mLessonCursor.close();
        }

        String[] projection = {
                LessonEntry.COL_HOUR,
                LessonEntry.COL_SUBJECT,
                LessonEntry.COL_LESSON_TYPE
        };

        String selection =
                LessonEntry.COL_DAY + " = " + mDayOfTheWeek + " AND " +
                LessonEntry.COL_HOUR + " = " + hour;

        String sortOrder = LessonEntry.COL_HOUR + " ASC, " + LessonEntry.COL_LESSON_NUM + " ASC";

        Uri uri = LessonEntry.CONTENT_URI;

        mLessonCursor = mContext.getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                sortOrder
        );
    }

    private void callScheduleCursor() {
        String projection[] = {
                ScheduleEntry._ID,
        };


        String selection = ScheduleEntry.COL_DAY + " = " + mDayOfTheWeek + " AND " +
                ScheduleEntry.COL_LESSON_COUNT + " != 0";

        Uri uri = ScheduleEntry.CONTENT_URI;

        mScheduleCursor = mContext.getContentResolver().query(
                uri,
                projection,
                selection,
                null, null
        );
    }
}

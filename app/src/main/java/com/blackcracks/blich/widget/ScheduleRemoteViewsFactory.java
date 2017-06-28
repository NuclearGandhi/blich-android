package com.blackcracks.blich.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.LessonEntry;
import com.blackcracks.blich.util.Constants;

import java.util.Calendar;

public class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;

    private Cursor mCursor;

    public ScheduleRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(Constants.Widget.EXTRA_WIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        if (mCursor != null) {
            mCursor.close();
        }

        String[] projection = {
                LessonEntry._ID,
                LessonEntry.COL_HOUR,
                LessonEntry.COL_LESSON_NUM,
                LessonEntry.COL_SUBJECT,
                LessonEntry.COL_LESSON_TYPE
        };

        Calendar calendar = Calendar.getInstance();
        String selection = LessonEntry.COL_DAY + " = " + calendar.get(Calendar.DAY_OF_WEEK);

        String sortOrder = LessonEntry.COL_HOUR + " ASC, " + LessonEntry.COL_LESSON_NUM + " ASC";

        Uri uri= LessonEntry.CONTENT_URI;

        mCursor = mContext.getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                sortOrder
        );
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (!mCursor.moveToPosition(position)) {
            return null;
        }

        int hour = mCursor.getInt(mCursor.getColumnIndex(LessonEntry.COL_HOUR));
        int lessonNum = mCursor.getInt(mCursor.getColumnIndex(LessonEntry.COL_LESSON_NUM));
        String subject = mCursor.getString(mCursor.getColumnIndex(LessonEntry.COL_SUBJECT));
        String lessonType = mContext.getString(mCursor.getColumnIndex(LessonEntry.COL_LESSON_TYPE));

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

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_item);

        views.setTextViewText(R.id.widget_schedule_hour, Integer.toString(hour));
        if (lessonNum == 0) views.setViewVisibility(R.id.widget_schedule_hour, View.VISIBLE);

        views.setTextViewText(R.id.widget_schedule_subject, subject);
        views.setTextColor(R.id.widget_schedule_subject, textColor);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor == null) return 0;
        return (long) mCursor.getInt(mCursor.getColumnIndex(LessonEntry._ID));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}

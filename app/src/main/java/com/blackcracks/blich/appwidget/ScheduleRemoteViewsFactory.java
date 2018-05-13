/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.appwidget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.DatedLesson;
import com.blackcracks.blich.data.schedule.ScheduleResult;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.adapter.helper.RealmScheduleHelper;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.List;

import io.realm.Realm;

@SuppressWarnings("ConstantConditions")
class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;

    private int mDay;
    private RealmScheduleHelper mRealmHelper;

    private int mPrimaryTextColor;
    private int mDividerColorResource;

    public ScheduleRemoteViewsFactory(Context context) {
        mContext = context;
        mRealmHelper = new RealmScheduleHelper(null);

        mDay = ScheduleUtils.getWantedDayOfTheWeek();
    }

    @Override
    public void onCreate() {
        RealmUtils.setUpRealm(mContext);
    }

    @Override
    public void onDataSetChanged() {
        PreferenceUtils.getInstance(mContext);
        Realm realm = Realm.getDefaultInstance();
        switchData(
                ScheduleUtils.fetchScheduleResult(
                        realm,
                        mDay,
                        true
                )
        );
        realm.close();

        updateTheme();
    }

    private void updateTheme() {
        String ateKey = Utilities.getATEKey(mContext);

        if (ateKey.equals("light_theme")) {
            mPrimaryTextColor = ContextCompat.getColor(mContext, R.color.text_color_primary_light);
            mDividerColorResource = R.color.divider_light;
        } else {
            mPrimaryTextColor = ContextCompat.getColor(mContext, R.color.text_color_primary_dark);
            mDividerColorResource = R.color.divider_dark;
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return mRealmHelper.getHourCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RawPeriod RawPeriod = mRealmHelper.getHour(position);
        int hourNum = mRealmHelper.getHour(position).getHour();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_appwidget_schedule);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(hourNum));

        views.setTextColor(R.id.widget_schedule_hour,
                mPrimaryTextColor);
        views.setImageViewResource(R.id.divider, mDividerColorResource);

        //Reset the views
        views.removeAllViews(R.id.widget_schedule_group);

        for (int i = 0; i < mRealmHelper.getChildCount(position); i++) {
            RawLesson rawLesson = mRealmHelper.getLesson(position, i);
            DatedLesson datedLesson;

            if (rawLesson == null) {//This is not a replacer DatedLesson
                List<DatedLesson> nonReplacingLessons = mRealmHelper.getAdditionalLessons(RawPeriod);
                int lastLessonPos = mRealmHelper.getLessonCount(position);
                int index = i - lastLessonPos;
                datedLesson = nonReplacingLessons.get(index);
            } else {
                datedLesson = mRealmHelper.getLessonReplacement(RawPeriod.getHour(), rawLesson);
            }

            //Data holders
            String subject;
            String teacher = "";
            int color;

            if (datedLesson != null) {
                subject = datedLesson.buildName();
                color = datedLesson.getColor();
            } else {
                subject = rawLesson.getSubject();
                teacher = rawLesson.getTeacher();
                color = mPrimaryTextColor;
            }

            Spanned text;
            if (!teacher.equals("")) {
                text = Html.fromHtml("<b>" + subject + "</b> - " + teacher);
            } else {
                text = Html.fromHtml("<b>" + subject + "</b>");
            }
            RemoteViews info = new RemoteViews(mContext.getPackageName(), R.layout.item_appwidget_lesson);
            info.setTextViewText(R.id.widget_schedule_subject, text);
            info.setTextColor(R.id.widget_schedule_subject, color);

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
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Switch the data being displayed in the widget.
     *
     * @param data data to switch to.
     */
    private void switchData(ScheduleResult data) {
        mRealmHelper.switchData(data);
    }
}

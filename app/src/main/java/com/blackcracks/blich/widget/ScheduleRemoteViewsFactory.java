/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.RealmScheduleHelper;
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
        Realm realm = Realm.getDefaultInstance();
        switchData(
                ScheduleUtils.fetchScheduleResult(
                        realm,
                        mContext,
                        mDay,
                        true
                )
        );
        realm.close();
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

        String ateKey = Utilities.getATEKey(mContext);

        Hour hour = mRealmHelper.getHour(position);
        int hourNum = mRealmHelper.getHour(position).getHour();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_item);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(hourNum));

        int primaryTextColor;
        if (ateKey.equals("light_theme")) {
            primaryTextColor = ContextCompat.getColor(mContext, R.color.black_text);
        } else {
            primaryTextColor = ContextCompat.getColor(mContext, R.color.white_text);
        }
        views.setTextColor(R.id.widget_schedule_hour,
                primaryTextColor);

        //Reset the views
        views.removeAllViews(R.id.widget_schedule_group);

        for (int i = 0; i < mRealmHelper.getChildCount(position); i++) {
            Lesson lesson = mRealmHelper.getLesson(position, i);
            DatedLesson datedLesson;

            if (lesson == null) {//This is not a replacer DatedLesson
                List<DatedLesson> nonReplacingLessons = mRealmHelper.getAdditionalLessons(hour);
                int lastLessonPos = mRealmHelper.getLessonCount(position) - 1;
                int index = i - lastLessonPos;
                datedLesson = nonReplacingLessons.get(index);
            } else {
                datedLesson = mRealmHelper.getLessonReplacement(hour.getHour(), lesson);
            }

            //Data holders
            String subject;
            String teacher = "";
            int color;

            if (datedLesson != null) {
                subject = datedLesson.buildName();
                color = datedLesson.getColor(mContext);
            } else {
                subject = lesson.getSubject();
                teacher = lesson.getTeacher();
                color = primaryTextColor;
            }

            Spanned text;
            if (!teacher.equals("")) {
                text = Html.fromHtml("<b>" + subject + "</b> - " + teacher);
            } else {
                text = Html.fromHtml("<b>" + subject + "</b>");
            }
            RemoteViews info = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_info);
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

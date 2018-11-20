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

package com.blackcracks.blich.appwidget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Period;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.List;

import io.realm.Realm;

@SuppressWarnings("ConstantConditions")
class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Realm mRealm;

    private List<Period> mData;
    private int mDay;

    private int mPrimaryTextColor;
    private int mDividerColorResource;

    public ScheduleRemoteViewsFactory(Context context) {
        mContext = context;

        mDay = ScheduleUtils.getWantedDayOfTheWeek();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mRealm == null) {
            RealmUtils.setUpRealm(mContext);
        }

        mRealm = Realm.getDefaultInstance();

        PreferenceUtils.getInstance(mContext);
        switchData(ScheduleUtils.fetchScheduleData(mRealm, mDay));
        updateTheme();
    }

    /**
     * Switch the data being displayed in the widget.
     *
     * @param data data to switch to.
     */
    private void switchData(List<Period> data) {
        mData = data;
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
        if (mRealm != null) {
            mRealm.close();
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Period period = mData.get(position);
        int periodNum = period.getPeriodNum();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_appwidget_schedule);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(periodNum));

        views.setTextColor(R.id.widget_schedule_hour,
                mPrimaryTextColor);
        views.setImageViewResource(R.id.divider, mDividerColorResource);

        //Reset the views
        views.removeAllViews(R.id.widget_schedule_group);

        for (int i = -1; i < period.getItemCount(); i++) {
            Lesson lesson;
            if (i == -1)
                lesson = period.getFirstLesson();
            else
                lesson = period.getItems().get(i);

            boolean isModified = lesson.getModifier() != null;
            //Data holders
            String subject = lesson.buildTitle();
            String teacher;
            if (isModified)
                teacher = "";
            else
                teacher = lesson.getTeacher();

            int color = lesson.getColor();
            if (color == -1)
                color = mPrimaryTextColor;

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
}

package com.blackcracks.blich.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

public class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Realm mRealm;

    private int mDay;
    private Utilities.Realm.RealmScheduleHelper mRealmHelper;

    public ScheduleRemoteViewsFactory(Context context) {
        mContext = context;
        mRealmHelper = new Utilities.Realm.RealmScheduleHelper(null);

        mDay = Utilities.Schedule.getWantedDayOfTheWeek();
    }

    @Override
    public void onCreate() {
        Utilities.Realm.setUpRealm(mContext);
    }

    @Override
    public void onDataSetChanged() {
        mRealm = Realm.getDefaultInstance();

        boolean isFilterOn = Utilities.getPrefBoolean(
                mContext,
                Constants.Preferences.PREF_FILTER_TOGGLE_KEY
        );

        List<Hour> data;
        if (isFilterOn) {
            List<Lesson> lessons = Utilities.Realm.getFilteredQuery(mRealm, mContext, Lesson.class, mDay)
                    .findAll();

            SparseIntArray hourArr = new SparseIntArray();
            for (int i = 0; i < lessons.size(); i++) {
                hourArr.append(i, lessons.get(i).getOwners().get(0).getHour());
            }

            lessons = mRealm.copyFromRealm(lessons);

            data = Utilities.Realm.convertLessonListToHour(lessons, mDay, hourArr);

            Comparator<Hour> hourComparator = new Comparator<Hour>() {
                @Override
                public int compare(Hour o1, Hour o2) {
                    if (o1.getHour() > o2.getHour()) return 1;
                    else if (o1.getHour() == o2.getHour()) return 0;
                    else return -1;
                }
            };

            Collections.sort(data, hourComparator);

        } else {
            data = mRealm.where(Hour.class)
                    .equalTo("day", mDay)
                    .sort("day", Sort.ASCENDING)
                    .findAll();

            data = mRealm.copyFromRealm(data);
        }
        switchData(data);
        mRealm.close();
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

        int hour = mRealmHelper.getHour(position).getHour();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_item);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(hour));

        views.removeAllViews(R.id.widget_schedule_group);


        for (int i = 0; i < mRealmHelper.getHour(position).getLessons().size(); i++) {
            Lesson lesson = mRealmHelper.getHour(position).getLessons().get(i);
            String subject = lesson.getSubject();
            String lessonType = lesson.getChangeType();

            int textColor = getColorFromType(lessonType);

            RemoteViews info = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_info);
            info.setTextViewText(R.id.widget_schedule_subject, subject);
            info.setTextColor(R.id.widget_schedule_subject, ContextCompat.getColor(mContext, textColor));

            views.addView(R.id.widget_schedule_group, info);
        }

        return views;
    }

    private int getColorFromType(String lessonType) {
        switch (lessonType) {
            case Constants.Database.TYPE_CANCELED:
                return R.color.lesson_canceled;
            case Constants.Database.TYPE_NEW_TEACHER:
                return R.color.lesson_changed;
            case Constants.Database.TYPE_EVENT:
                return R.color.lesson_event;
            case Constants.Database.TYPE_EXAM:
                return R.color.lesson_exam;
            default:
                return R.color.black_text;
        }
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

    private void switchData(List<Hour> data) {
        ScheduleResult result = new ScheduleResult(
                data,
                new ArrayList<Change>()
        );
        mRealmHelper.switchData(result);

    }
}

package com.blackcracks.blich.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;
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

        Calendar calendar = Calendar.getInstance();
        mDay = calendar.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public void onCreate() {
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDataSetChanged() {

        boolean isFilterOn = Utilities.getPrefBoolean(
                mContext,
                Constants.Preferences.PREF_FILTER_TOGGLE_KEY
        );

        List<Hour> data;
        if (isFilterOn) {
            List<Lesson> lessons = Utilities.Realm.getFilteredLessonsQuery(mRealm, mContext, mDay)
                    .findAll()
                    .sort("owners.day", Sort.ASCENDING);

            data = Utilities.Realm.convertLessonListToHour(lessons, mDay);


        } else {
            data = mRealm.where(Hour.class)
                    .equalTo("day", mDay)
                    .findAll();
        }

        mRealmHelper.switchData(data);
    }

    @Override
    public void onDestroy() {
        mRealm.close();
    }

    @Override
    public int getCount() {
        int hourCount = mRealmHelper.getHourCount();
        int lessonCount = 0;
        for(int i = 0; i < hourCount; i++) {
            lessonCount += mRealmHelper.getHour(i).getLessons().size();
        }

        return lessonCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        int hour = mRealmHelper.getHour(position).getHour();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_item);
        views.setTextViewText(
                R.id.widget_schedule_hour,
                Integer.toString(hour));

        views.removeAllViews(R.id.widget_schedule_group);

        for (int i = 0; i < mRealmHelper.getHourCount(); i++) {
            Lesson lesson = mRealmHelper.getHour(i).getLessons().get(i);
            String subject = lesson.getSubject();
            String lessonType = lesson.getChangeType();

            int textColor = getColorFromType(lessonType);

            RemoteViews info = new RemoteViews(mContext.getPackageName(), R.layout.widget_schedule_info);
            info.setTextViewText(R.id.widget_schedule_subject, subject);
            info.setTextColor(R.id.widget_schedule_subject, textColor);

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
        return 0;
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

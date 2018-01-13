package com.blackcracks.blich.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Utilities;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Realm mRealm;

    private int mDay;
    private RealmWidgetHelper mRealmHelper;

    public ScheduleRemoteViewsFactory(Context context, int day) {
        mContext = context;
        mDay = day;
        mRealmHelper = new RealmWidgetHelper(null);
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

        RealmResults<Lesson> data;
        if (isFilterOn) {
            data = Utilities.Realm.getFilteredLessonsQuery(mRealm, mContext, mDay)
                    .findAll();
        } else {
            data = mRealm.where(Lesson.class)
                    .equalTo("owners.day", mDay)
                    .findAll();
        }

        data.sort("owners.day", Sort.ASCENDING);

        mRealmHelper.switchData(data);
    }

    @Override
    public void onDestroy() {
        mRealm.close();
    }

    @Override
    public int getCount() {
        return mRealmHelper.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        return null;
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

    private class RealmWidgetHelper {

        private List<Lesson> mData;
        private boolean mIsDataValid;

        RealmWidgetHelper(List<Lesson> data) {
            switchData(data);
        }

        public void switchData(List<Lesson> data) {
            mData = data;
            if (mData == null) mIsDataValid = false;
        }

        public Lesson getLesson(int position) {
            if (!mIsDataValid) return null;
            return mData.get(position);
        }

        public int getCount() {
            if (!mIsDataValid) return 0;
            return mData.size();
        }
    }
}

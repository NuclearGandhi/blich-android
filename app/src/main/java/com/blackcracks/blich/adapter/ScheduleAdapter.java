package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichDatabase;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends BaseExpandableListAdapter {

    private int mDay;

    private QueryEnumeratorHelper mQueryHelper;
    private Context mContext;
    private TextView mStatusTextView;

    public ScheduleAdapter(Context context,
                           int day,
                           TextView statusTextView) {
        mContext = context;
        mDay = day;

        mQueryHelper = new QueryEnumeratorHelper(null);
    }

    @Override
    public int getGroupCount() {
        int count = mQueryHelper.getHourCount();
        if (count == 0) {
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }
        return count;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mQueryHelper.getChildCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mQueryHelper.getHour(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mQueryHelper.getLesson(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void setData(QueryEnumerator enumerator) {
        mQueryHelper.setData(enumerator);
        notifyDataSetChanged();
    }

    private View makeEventDot(ViewGroup parent, @ColorRes int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_event_dot, parent, false);

        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.events_dot);
        drawable.setColor(ContextCompat.getColor(mContext, color));
        view.setBackground(drawable);

        return view;
    }

    private static class GroupViewHolder {

        private final LinearLayout eventsView;
        private final TextView hourView;
        private final TextView subjectsView;
        private final TextView teacherView;
        private final TextView classroomView;
        private final ImageView indicatorView;

        GroupViewHolder(View view) {
            eventsView = view.findViewById(R.id.schedule_group_events);
            hourView = view.findViewById(R.id.schedule_group_hour);
            subjectsView = view.findViewById(R.id.schedule_group_subject);
            teacherView = view.findViewById(R.id.schedule_group_teacher);
            classroomView = view.findViewById(R.id.schedule_group_classroom);
            indicatorView = view.findViewById(R.id.schedule_group_indicator);
        }
    }

    private static class ChildViewHolder {
        private final TextView subjectView;
        private final TextView classroomView;
        private final TextView teacherView;

        private ChildViewHolder(View view) {
            subjectView = view.findViewById(R.id.schedule_child_subject);
            classroomView = view.findViewById(R.id.schedule_child_classroom);
            teacherView = view.findViewById(R.id.schedule_child_teacher);
        }
    }

    private class QueryEnumeratorHelper {
        private QueryEnumerator mData;
        private List<Hour> mHours = new ArrayList<>();
        private boolean mIsDataValid;

        QueryEnumeratorHelper(QueryEnumerator data) {
            mData = data;
            mIsDataValid = data != null;
        }

        void setData(QueryEnumerator data) {
            mData = data;

            if (data != null) mIsDataValid = true;
        }

        boolean isDataValid() {
            return mIsDataValid;
        }

        Hour getHour(int position) {

            if (!mIsDataValid) return null;
            if (mHours.get(position) == null) {
                QueryRow row = mData.getRow(position);

                List<Lesson> lessons = new ArrayList<>();
                List<Map<String, Object>> filtered = (List<Map<String, Object>>) row.getValue();

                for (Map<String, Object> queriedLesson:
                        filtered) {

                    String subject = (String) queriedLesson.get(BlichDatabase.SUBJECT_KEY);
                    String teacher = (String) queriedLesson.get(BlichDatabase.TEACHER_KEY);
                    String classroom = (String) queriedLesson.get(BlichDatabase.CLASSROOM_KEY);
                    String lessonType = (String) queriedLesson.get(BlichDatabase.LESSON_TYPE_KEY);
                    Lesson lesson = new Lesson(subject, teacher, classroom, lessonType);

                    lessons.add(lesson);
                }

                String queriedHour = (String) row.getKey();
                int hourNum = Integer.parseInt(queriedHour.replace(BlichDatabase.HOUR_KEY, ""));

                Hour hour = new Hour(hourNum, lessons);
                mHours.add(position, hour);
                return hour;
            }
            return mHours.get(position);
        }

        Lesson getLesson(int position, int childPos) {
            if (mIsDataValid) return null;
            Hour hour = getHour(position);
            return hour.getLessons().get(childPos);
        }

        int getHourCount() {
            if (mIsDataValid) {
                return mData.getCount();
            } else {
                return 0;
            }
        }

        int getChildCount(int position) {
            if (mIsDataValid) {
                return getHour(position).getLessons().size();
            } else {
                return 0;
            }
        }
    }
}

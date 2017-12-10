package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends BaseExpandableListAdapter implements
        ExpandableListView.OnGroupExpandListener,
        ExpandableListView.OnGroupCollapseListener{

    private ExpandableListView mExpandableListView;
    private QueryEnumeratorHelper mQueryHelper;
    private Context mContext;
    private TextView mStatusTextView;
    private SparseBooleanArray mExpandedArray;
    private SparseArray<GroupViewHolder> mViewHolderSparseArray;

    public ScheduleAdapter(ExpandableListView expandableListView,
                           Context context,
                           TextView statusTextView) {
        mContext = context;
        mStatusTextView = statusTextView;
        mExpandableListView = expandableListView;

        mExpandableListView.setOnGroupExpandListener(this);
        mExpandableListView.setOnGroupCollapseListener(this);

        mQueryHelper = new QueryEnumeratorHelper(null);
        mExpandedArray = new SparseBooleanArray();
        mViewHolderSparseArray = new SparseArray<>();
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
        return mQueryHelper.getChildCount(groupPosition) - 1;
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
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (!mQueryHelper.isDataValid()) return null;

        View view;
        if (convertView == null) {
            view = newGroupView(parent);
        } else {
            view = convertView;
            GroupViewHolder holder = new GroupViewHolder(view);
            view.setTag(holder);
        }
        bindGroupView(groupPosition, view);
        return view;
    }

    private View newGroupView(ViewGroup parent) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.schedule_group, parent, false);
        GroupViewHolder holder = new GroupViewHolder(view);
        view.setTag(holder);
        return view;
    }

    private void bindGroupView(int groupPosition, View view) {

        final GroupViewHolder holder = (GroupViewHolder) view.getTag();
        Hour hour = (Hour) getGroup(groupPosition);
        holder.hourView.setText(hour.getHour() + "");

        List<Lesson> lessons = hour.getLessons();
        Lesson first_lesson = lessons.get(0);
        String subject = first_lesson.getSubject();
        final String teacher = first_lesson.getTeacher();
        final String classroom = first_lesson.getClassroom();

        holder.subjectView.setText(subject);
        holder.teacherView.setText("...");
        holder.classroomView.setText("");
        holder.indicatorView.setRotationX(0);

        //Set the color according to the lesson type
        int color = getColorFromType(first_lesson.getLessonType());
        holder.subjectView.setTextColor(ContextCompat.getColor(mContext, color));

        holder.eventsView.removeAllViews();
        holder.eventsView.setVisibility(View.VISIBLE);
        //Add dots to signify that there are changes
        List<String> existingTypes = new ArrayList<>();
        for(int i = 1 ; i < lessons.size(); i++) {
            //Begin from 1, no need to signify about an event already shown to the user
            Lesson lesson = lessons.get(i);
            existingTypes.add(lesson.getLessonType());
        }

        if (existingTypes.contains(BlichDatabase.TYPE_CANCELED)) makeEventDot(holder.eventsView, R.color.lesson_canceled);
        if (existingTypes.contains(BlichDatabase.TYPE_CHANGE)) makeEventDot(holder.eventsView, R.color.lesson_changed);
        if (existingTypes.contains(BlichDatabase.TYPE_EXAM)) makeEventDot(holder.eventsView, R.color.lesson_exam);
        if (existingTypes.contains(BlichDatabase.TYPE_EVENT)) makeEventDot(holder.eventsView, R.color.lesson_event);

        if (getChildrenCount(groupPosition) == 0) {
            holder.indicatorView.setVisibility(View.GONE);
            holder.teacherView.setText(teacher);
            holder.classroomView.setText(classroom);
            view.setOnClickListener(null);
        } else {
            holder.indicatorView.setVisibility(View.VISIBLE);
            final int finalGroupPos = groupPosition;
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean isExpanded = mExpandedArray.get(finalGroupPos, false);
                    if (!isExpanded) {
                        //Expand
                        mExpandableListView.expandGroup(finalGroupPos, true);
                        mExpandedArray.put(finalGroupPos, true);
                    } else {
                        //Collapse
                        mExpandableListView.collapseGroup(finalGroupPos);
                        mExpandedArray.put(finalGroupPos, false);
                    }
                }
            });
        }
        mViewHolderSparseArray.put(groupPosition, holder);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (!mQueryHelper.isDataValid()) return null;

        View view;
        if (convertView == null) {
            view = newChildView(parent);
        } else {
            view = convertView;
        }
        bindChildView(groupPosition, childPosition, view);
        return view;
    }

    private View newChildView(ViewGroup parent) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.schedule_child, parent, false);
        ChildViewHolder holder = new ChildViewHolder(view);
        view.setTag(holder);
        return view;
    }

    private void bindChildView(int groupPosition, int childPosition, View view) {
        ChildViewHolder holder = (ChildViewHolder) view.getTag();
        Lesson lesson = (Lesson) getChild(groupPosition, childPosition + 1);

        holder.subjectView.setText(lesson.getSubject());
        holder.teacherView.setText(lesson.getTeacher());
        holder.classroomView.setText(lesson.getClassroom());

        int color = getColorFromType(lesson.getLessonType());
        holder.subjectView.setTextColor(ContextCompat.getColor(mContext, color));

        //Set a bottom divider if this is the last child
        View divider = view.findViewById(R.id.divider);
        if (getChildrenCount(groupPosition) - 1 == childPosition) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }

    }

    @Override
    public void onGroupExpand(int groupPosition) {
        final GroupViewHolder holder = mViewHolderSparseArray.get(groupPosition);
        final Lesson lesson = (Lesson) getChild(groupPosition, 0);
        holder.indicatorView.animate().rotation(180);
        holder.teacherView.post(new Runnable() {
            @Override
            public void run() {
                holder.teacherView.setText(lesson.getTeacher());
                holder.classroomView.setText(lesson.getClassroom());
                holder.eventsView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        GroupViewHolder holder = mViewHolderSparseArray.get(groupPosition);
        holder.indicatorView.animate().rotation(0);
        holder.teacherView.setText("...");
        holder.classroomView.setText("");

        holder.eventsView.setVisibility(View.VISIBLE);
    }

    public void switchData(QueryEnumerator enumerator) {
        mQueryHelper.switchData(enumerator);
        mExpandedArray.clear();
        notifyDataSetChanged();
    }

    private void makeEventDot(ViewGroup parent, @ColorRes int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_event_dot, parent, false);

        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.events_dot);
        drawable.setColor(ContextCompat.getColor(mContext, color));
        view.setBackground(drawable);

        parent.addView(view);
    }

    private int getColorFromType(String lessonType) {
        switch (lessonType) {
            case BlichDatabase.TYPE_CANCELED:
                return R.color.lesson_canceled;
            case BlichDatabase.TYPE_CHANGE:
                return R.color.lesson_changed;
            case BlichDatabase.TYPE_EVENT:
                return R.color.lesson_event;
            case BlichDatabase.TYPE_EXAM:
                return R.color.lesson_exam;
            default:
                return R.color.black_text;
        }
    }

    private static class GroupViewHolder {

        final LinearLayout eventsView;
        final TextView hourView;
        final TextView subjectView;
        final TextView teacherView;
        final TextView classroomView;
        final ImageView indicatorView;

        GroupViewHolder(View view) {
            eventsView = view.findViewById(R.id.schedule_group_events);
            hourView = view.findViewById(R.id.schedule_group_hour);
            subjectView = view.findViewById(R.id.schedule_group_subject);
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
            switchData(data);
        }

        private void sortData() {
            if (mIsDataValid) {
                for (int i = 0; i < mData.getCount(); i++) {
                    getHour(i);
                }
                Collections.sort(mHours);
            }
        }

        void switchData(QueryEnumerator data) {
            mData = data;
            mIsDataValid = data != null;
            sortData();
        }

        boolean isDataValid() {
            return mIsDataValid;
        }

        Hour getHour(int position) {

            if (!mIsDataValid) return null;
            if (mHours.size() != mData.getCount()) {
                QueryRow row = mData.getRow(position);

                List<Lesson> lessons = new ArrayList<>();
                List<Map<String, Object>> filtered = (List<Map<String, Object>>) row.getValue();

                for (Map<String, Object> queriedLesson :
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
            if (!mIsDataValid) return null;
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

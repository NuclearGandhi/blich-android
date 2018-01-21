package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
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
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.Utilities;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends BaseExpandableListAdapter{

    private ExpandableListView mExpandableListView;
    private Utilities.Realm.RealmScheduleHelper mRealmScheduleHelper;
    private Context mContext;
    private TextView mStatusTextView;
    private SparseBooleanArray mExpandedArray;

    public ScheduleAdapter(ExpandableListView expandableListView,
                           Context context,
                           TextView statusTextView) {
        mContext = context;
        mStatusTextView = statusTextView;
        mExpandableListView = expandableListView;

        mRealmScheduleHelper = new Utilities.Realm.RealmScheduleHelper(null);
        mExpandedArray = new SparseBooleanArray();
    }

    @Override
    public int getGroupCount() {
        int count = mRealmScheduleHelper.getHourCount();
        if (count == 0) {
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }
        return count;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mRealmScheduleHelper.getChildCount(groupPosition) - 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mRealmScheduleHelper.getHour(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mRealmScheduleHelper.getLesson(groupPosition, childPosition);
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
        if (!mRealmScheduleHelper.isDataValid()) return null;

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

        final List<Lesson> lessons = hour.getLessons();
        final Lesson first_lesson = lessons.get(0);

        String subject = filterSubject(first_lesson.getSubject());
        final String teacher = first_lesson.getTeacher();
        final String room = first_lesson.getRoom();

        //Set initial values
        holder.subjectView.setText(subject);
        holder.teacherView.setText("...");
        holder.classroomView.setText("");
        holder.indicatorView.setRotationX(0);

        //Set the color according to the lesson type
        int color = getColorFromType(first_lesson.getChangeType());
        holder.subjectView.setTextColor(ContextCompat.getColor(mContext, color));

        holder.eventsView.removeAllViews();
        holder.eventsView.setVisibility(View.VISIBLE);
        //Add dots to signify that there are changes
        List<String> existingTypes = new ArrayList<>();
        List<Change> changes = mRealmScheduleHelper.getChanges(hour.getHour());

        for(int i = 1 ; i < changes.size(); i++) {
            //Begin from 1, no need to signify about an event already shown to the user
            Change change = changes.get(i);
            existingTypes.add(change.getChangeType());
        }

        if (existingTypes.contains(Database.TYPE_CANCELED)) makeEventDot(holder.eventsView, R.color.lesson_canceled);
        if (existingTypes.contains(Database.TYPE_NEW_TEACHER)) makeEventDot(holder.eventsView, R.color.lesson_changed);
        if (existingTypes.contains(Database.TYPE_EXAM)) makeEventDot(holder.eventsView, R.color.lesson_exam);
        if (existingTypes.contains(Database.TYPE_EVENT)) makeEventDot(holder.eventsView, R.color.lesson_event);

        if (mExpandedArray.get(groupPosition)) {
            showExpandedGroup(holder, teacher, room);
        } else {
            showCollapsed(holder);
        }

        //Set values according to the amount of children in this hour
        if (getChildrenCount(groupPosition) == 0) {
            holder.indicatorView.setVisibility(View.GONE);
            holder.teacherView.setText(teacher);
            holder.classroomView.setText(room);
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
                        showExpandedGroup(holder, teacher, room);
                    } else {
                        //Collapse
                        mExpandableListView.collapseGroup(finalGroupPos);
                        mExpandedArray.put(finalGroupPos, false);
                        showCollapsed(holder);
                    }
                }
            });
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (!mRealmScheduleHelper.isDataValid()) return null;

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
        Change change = mRealmScheduleHelper.getChange(
                mRealmScheduleHelper.getHour(groupPosition).getHour(),
                lesson
        );

        int color;
        if (change == null) {
            String subject = filterSubject(lesson.getSubject());
            holder.subjectView.setText(subject);

            holder.teacherView.setText(lesson.getTeacher());
            holder.classroomView.setText(lesson.getRoom());

            color = R.color.black_text;
        } else {
            holder.subjectView.setText(change.buildSubject());
            holder.teacherView.setText("");
            holder.classroomView.setText("");

            color = getColorFromType(change.getChangeType());
        }

        holder.subjectView.setTextColor(ContextCompat.getColor(mContext, color));

        //Set a bottom divider if this is the last child
        View divider = view.findViewById(R.id.divider);
        if (getChildrenCount(groupPosition) - 1 == childPosition) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }
    }

    private String filterSubject(String subject) {
        if (subject.contains("מבחן מבחן")
                || subject.contains("מבחן בוחן")
                || subject.contains("מבחן מבחני")) {
            return subject.replace("מבחן ", "");
        } else {
            return subject;
        }
    }

    private void makeEventDot(ViewGroup parent, @ColorRes int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_event_dot, parent, false);

        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.events_dot);
        drawable.setColor(ContextCompat.getColor(mContext, color));
        view.setBackground(drawable);

        parent.addView(view);
    }

    private void showExpandedGroup(final GroupViewHolder holder, final String teacher, final String room) {
        holder.indicatorView.animate().rotation(180);

        holder.teacherView.post(new Runnable() {
            @Override
            public void run() {
                holder.teacherView.setText(teacher);
                holder.classroomView.setText(room);
                holder.eventsView.setVisibility(View.GONE);
            }
        });
    }

    private void showCollapsed(GroupViewHolder holder) {
        holder.indicatorView.animate().rotation(0);
        holder.teacherView.setText("...");
        holder.classroomView.setText("");

        holder.eventsView.setVisibility(View.VISIBLE);
    }

    private int getColorFromType(String lessonType) {
        switch (lessonType) {
            case Database.TYPE_CANCELED:
                return R.color.lesson_canceled;
            case Database.TYPE_NEW_TEACHER:
                return R.color.lesson_changed;
            case Database.TYPE_EVENT:
                return R.color.lesson_event;
            case Database.TYPE_EXAM:
                return R.color.lesson_exam;
            default:
                return R.color.black_text;
        }
    }

    public void switchData(ScheduleResult data) {
        mRealmScheduleHelper.switchData(data);
        mExpandedArray.clear();
        notifyDataSetChanged();
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
}

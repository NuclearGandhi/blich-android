/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
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

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.RealmScheduleHelper;
import com.blackcracks.blich.util.ScheduleUtils;

import java.util.List;

public class ScheduleAdapter extends BaseExpandableListAdapter {

    private RealmScheduleHelper mRealmScheduleHelper;
    private Context mContext;

    private ExpandableListView mExpandableListView;
    private TextView mStatusTextView;

    private SparseBooleanArray mExpandedArray;

    /**
     * @param expandableListView the corresponding view for the adapter.
     * @param statusTextView {@link TextView} for when the data is invalid.
     */
    public ScheduleAdapter(ExpandableListView expandableListView,
                           Context context,
                           TextView statusTextView) {
        mRealmScheduleHelper = new RealmScheduleHelper(null);
        mContext = context;

        mExpandableListView = expandableListView;
        mStatusTextView = statusTextView;

        mExpandedArray = new SparseBooleanArray();
    }

    @Override
    public int getGroupCount() {
        int count = mRealmScheduleHelper.getHourCount();
        //Set the status TextView according to the validity of the data.
        if (count == 0) mStatusTextView.setVisibility(View.VISIBLE);
        else mStatusTextView.setVisibility(View.GONE);

        return count;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int count = mRealmScheduleHelper.getChildCount(groupPosition);
        /*The number of group children is smaller by one than the number of lessons. This is because
        one lesson is already displayed in the group view.
         */
        if (count != 0) count--;
        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mRealmScheduleHelper.getHour(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mRealmScheduleHelper.getLesson(groupPosition, childPosition + 1);
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
        //Set initial values
        holder.reset();

        //Get the overall data object
        Hour hour = (Hour) getGroup(groupPosition);
        int hourNum = hour.getHour();

        //Set the hour indicator text
        String hourText = hourNum + "";
        holder.hourView.setText(hourText);
        if (((MainActivity) mContext).getATEKey().equals("dark_theme")) holder.hourView.setBackground(null);

        //Get all the lessons and events
        final List<Lesson> lessons = hour.getLessons();

        Lesson firstLesson = null;
        DatedLesson singleChild = mRealmScheduleHelper.getNonReplacingLesson(hour);
        DatedLesson replacement = null;

        //The main data
        String subject;
        final String teacher;
        final String room;
        int color;

        /*
        There are 4 types of children:
        -Lesson
        -Change (replaces Lesson)
        -Event (in addition to Lesson)
        -Exam (in addition to Lesson)
         */
        if (singleChild != null) { //Then display the single lesson
            subject = singleChild.buildName();
            teacher = "";
            room = "";
            color = singleChild.getColor();
        } else {
            if (lessons == null) {
                replacement = mRealmScheduleHelper.getAdditionalLessons(hour).get(0);
            } else {
                firstLesson = lessons.get(0); //We need to display the first lesson in the group collapsed mode
                replacement = mRealmScheduleHelper.getLessonReplacement(hourNum, firstLesson);
            }

            if (replacement == null) { //Then display a normal lesson
                subject = firstLesson.getSubject();
                teacher = firstLesson.getTeacher();
                room = firstLesson.getRoom();
                color = Config.textColorPrimary(mContext, null);
            } else { //Then display a modified lesson
                subject = replacement.buildName();
                teacher = "";
                room = "";
                color = replacement.getColor();
            }
        }

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);

        //Add dots to signify that there are changes
        if (singleChild == null) {
            List<DatedLesson> datedLessons = mRealmScheduleHelper.getDatedLessons(hour); //Get all the dated lessons
            ScheduleUtils.removeDuplicateDaterLessons(datedLessons); //Remove duplicates
            datedLessons.remove(replacement); //Remove the displayed dated lesson
            makeEventDots(holder.eventsView, datedLessons); //Load the data into the event dots
        }

        //Display the correct state of the group
        if (mExpandedArray.get(groupPosition)) {
            showExpandedGroup(holder, teacher, room);
        } else {
            showCollapsed(holder);
        }

        /*
        If there are no children, show a simple item.
        Else, show the indicator view and add a click listener
         */
        if (getChildrenCount(groupPosition) == 0) {
            if (teacher.equals("")) holder.teacherView.setVisibility(View.GONE);
            holder.teacherView.setText(teacher);
            holder.classroomView.setText(room);
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

    private void showExpandedGroup(final GroupViewHolder holder, final String teacher, final String room) {
        holder.indicatorView.animate().rotation(180);

        //Run on a different thread due to a bug where the view won't update itself if modified from this thread
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
        holder.reset();

        Lesson lesson = (Lesson) getChild(groupPosition, childPosition);
        DatedLesson datedLesson;

        Hour hour = (Hour) getGroup(groupPosition);
        if (lesson == null) {//This is not a replacer DatedLesson, therefore get the non replacing lesson
            List<DatedLesson> nonReplacingLessons = mRealmScheduleHelper.getAdditionalLessons(hour);
            int lastLessonPos = mRealmScheduleHelper.getLessonCount(groupPosition) - 1;
            int index = childPosition - lastLessonPos;
            datedLesson = nonReplacingLessons.get(index);
        } else {
            datedLesson = mRealmScheduleHelper.getLessonReplacement(hour.getHour(), lesson);
        }
        String subject;
        final String teacher;
        final String room;
        int color;

        if (datedLesson != null) {//Apply DatedLesson
            subject = datedLesson.buildName();
            teacher = "";
            room = "";
            color = datedLesson.getColor();
        } else {//Normal lesson
            //noinspection ConstantConditions
            subject = lesson.getSubject();
            teacher = lesson.getTeacher();
            room = lesson.getRoom();
            color = Config.textColorPrimary(mContext, null);
        }

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);

        if (teacher.equals("")) holder.teacherView.setVisibility(View.GONE);
        holder.teacherView.setText(teacher);
        holder.classroomView.setText(room);

        //Set a bottom divider if this is the last child
        View divider = view.findViewById(R.id.divider);
        if (getChildrenCount(groupPosition) - 1 == childPosition) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }
    }

    /**
     * Add event dots to the parent view.
     *
     * @param parent the view to put the dots in.
     * @param list list of {@link DatedLesson}s to make event dots from.
     */
    private void makeEventDots(ViewGroup parent, List<DatedLesson> list) {
        for (DatedLesson lesson :
                list) {
            makeEventDot(parent, lesson.getColor());
        }
    }

    /**
     * Add a new dot to the parent view.
     *
     * @param parent a parent {@link ViewGroup}
     * @param color the color of the desired dot.
     */
    private void makeEventDot(ViewGroup parent, @ColorInt int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_event_dot, parent, false);

        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.event_dot);
        //noinspection ConstantConditions
        drawable.setColor(color);
        view.setBackground(drawable);
        parent.addView(view);
    }

    /**
     * Switch the schedule to be displayed. Calls {@link BaseExpandableListAdapter#notifyDataSetChanged()}.
     *
     * @param data data to switch to.
     */
    public void switchData(ScheduleResult data) {
        mRealmScheduleHelper.switchData(data);
        mExpandedArray.clear();
        notifyDataSetChanged();
    }

    /**
     * A class to hold all references to each group's child views.
     */
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

        void reset() {
            subjectView.setText("");

            teacherView.setVisibility(View.VISIBLE);
            teacherView.setText("...");

            classroomView.setText("");
            indicatorView.setRotationX(0);
            indicatorView.setVisibility(View.GONE);

            eventsView.removeAllViews();
            eventsView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A class to hold all references to each child's child views.
     */
    private static class ChildViewHolder {
        private final TextView subjectView;
        private final TextView classroomView;
        private final TextView teacherView;

        private ChildViewHolder(View view) {
            subjectView = view.findViewById(R.id.schedule_child_subject);
            classroomView = view.findViewById(R.id.schedule_child_classroom);
            teacherView = view.findViewById(R.id.schedule_child_teacher);
        }

        void reset() {
            teacherView.setVisibility(View.VISIBLE);
        }
    }
}

/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.schedule.DatedLesson;
import com.blackcracks.blich.data.raw.Hour;
import com.blackcracks.blich.data.raw.Lesson;
import com.blackcracks.blich.data.schedule.ScheduleResult;
import com.blackcracks.blich.adapter.helper.RealmScheduleHelper;
import com.blackcracks.blich.util.ScheduleUtils;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ScheduleAdapter extends ExpandableRecyclerViewAdapter<ScheduleAdapter.GroupViewHolder, ScheduleAdapter.ChildViewHolder> {

    private RealmScheduleHelper mRealmScheduleHelper;
    private Context mContext;
    private String mAteKey;

    private SparseBooleanArray mExpandedArray;

    public ScheduleAdapter(List<? extends ExpandableGroup> groups,
                           Context context) {
        super(groups);

        mRealmScheduleHelper = new RealmScheduleHelper(null);
        mContext = context;

        mAteKey = ((MainActivity) mContext).getATEKey();

        mExpandedArray = new SparseBooleanArray();
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        if (!mRealmScheduleHelper.isDataValid()) return null;

        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_schedule_group, parent, false);
        GroupViewHolder holder = new GroupViewHolder(view);
        return holder;
    }

    @Override
    public void onBindGroupViewHolder(final GroupViewHolder holder,
                                      final int groupPosition,
                                      ExpandableGroup group) {
        //Set initial values
        holder.reset();

        //Get the overall data object
        Hour hour = (Hour) getGroup(groupPosition);
        int hourNum = hour.getHour();

        //Set the hour indicator text
        String hourText = hourNum + "";
        holder.hourView.setText(hourText);
        if (mAteKey.equals("dark_theme"))
            holder.hourView.setBackground(null);
        else
            holder.hourView.setTextColor(Config.textColorPrimaryInverse(mContext, mAteKey));

        //Get all the lessons and events
        final List<Lesson> lessons = hour.getLessons();

        Lesson firstLesson = null;
        DatedLesson specialEvent = mRealmScheduleHelper.getNonReplacingLesson(hour);
        DatedLesson replacement = null;

        //The main data
        String subject;
        final String teacher;
        final String room;
        int color;

        boolean isModified = false;

        /*
        There are 4 types of children:
        -Lesson
        -Change (replaces Lesson)
        -Event (in addition to Lesson)
        -Exam (in addition to Lesson)
         */
        if (specialEvent != null) { //Then display the single lesson
            subject = specialEvent.buildName();
            teacher = "";
            room = "";
            color = specialEvent.getColor();

            isModified = true;
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

                isModified = true;
            }
        }

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);
        holder.setData(teacher, room);

        //Add dots to signify that there are changes
        if (specialEvent == null) {
            List<DatedLesson> datedLessons = mRealmScheduleHelper.getDatedLessons(hour); //Get all the dated lessons
            ScheduleUtils.removeDuplicateDaterLessons(datedLessons); //Remove duplicates
            datedLessons.remove(replacement); //Remove the displayed dated lesson
            makeEventDots(holder.eventsView, datedLessons); //Load the data into the event dots
        }


        if (isModified)
            setSingleLine((ConstraintLayout) holder.subjectView.getParent());

        boolean isSingleLesson = getChildrenCount(groupPosition) == 0;

        //Display the correct state of the group
        if (mExpandedArray.get(groupPosition) || isSingleLesson) {
            holder.expand();
        } else {
            holder.collapse();
        }
        /*
        If there are no children, show a simple item.
        Else, show the indicator view and add a click listener
         */
        if (isSingleLesson) {
            holder.teacherView.setText(teacher);
            holder.classroomView.setText(room);
        } else {
            holder.indicatorView.setVisibility(View.VISIBLE);
            holder.setOnGroupClickListener(new OnGroupClickListener() {
                @Override
                public boolean onGroupClick(int flatPos) {
                    boolean isExpanded = mExpandedArray.get(flatPos, false);
                    if (!isExpanded) {
                        //Expand
                        mExpandedArray.put(flatPos, true);
                        return true;
                    } else {
                        //Collapse
                        mExpandedArray.put(flatPos, false);
                        return false;
                    }
                }
            });
        }
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_schedule_child, parent, false);
        ChildViewHolder holder = new ChildViewHolder(view);
        return holder;
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder,
                                      int groupPosition,
                                      ExpandableGroup group,
                                      int childPosition) {
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

        boolean isModified = false;

        if (datedLesson != null) {//Apply DatedLesson
            subject = datedLesson.buildName();
            teacher = "";
            room = "";
            color = datedLesson.getColor();

            isModified = true;
        } else {//Normal lesson
            //noinspection ConstantConditions
            subject = lesson.getSubject();
            teacher = lesson.getTeacher();
            room = lesson.getRoom();
            color = Config.textColorPrimary(mContext, null);
        }

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);

        if (isModified)
            setSingleLine((ConstraintLayout) holder.subjectView.getParent());

        holder.teacherView.setText(teacher);
        holder.classroomView.setText(room);

        //Set a bottom divider if this is the last child
        if (getChildrenCount(groupPosition) - 1 == childPosition) {
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.divider.setVisibility(View.GONE);
        }
    }

    public int getChildrenCount(int groupPosition) {
        int count = mRealmScheduleHelper.getChildCount(groupPosition);
        /*The number of group children is smaller by one than the number of lessons. This is because
        one lesson is already displayed in the group view.
         */
        if (count != 0) count--;
        return count;
    }

    public Object getGroup(int groupPosition) {
        return mRealmScheduleHelper.getHour(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return mRealmScheduleHelper.getLesson(groupPosition, childPosition + 1);
    }

    /**
     * Add event dots to the parent view.
     *
     * @param parent the view to put the dots in.
     * @param list   list of {@link DatedLesson}s to make event dots from.
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
     * @param color  the color of the desired dot.
     */
    private void makeEventDot(ViewGroup parent, @ColorInt int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dot_modified_lesson, parent, false);

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
    static class GroupViewHolder extends com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder {

        private final LinearLayout eventsView;
        private final TextView hourView;
        private final TextView subjectView;
        private final TextView teacherView;
        private final TextView classroomView;
        private final ImageView indicatorView;

        private String teacher;
        private String room;

        GroupViewHolder(View view) {
            super(view);
            eventsView = view.findViewById(R.id.item_event_dots);
            hourView = view.findViewById(R.id.item_hour_num);
            subjectView = view.findViewById(R.id.item_subject);
            teacherView = view.findViewById(R.id.item_teacher);
            classroomView = view.findViewById(R.id.item_classroom);
            indicatorView = view.findViewById(R.id.item_expand_indicator);
        }

        public void setData(String teacher, String room) {
            this.teacher = teacher;
            this.room = room;
        }

        @Override
        public void expand() {
            super.expand();
            indicatorView.animate().rotation(180);

            //Run on a different thread due to a bug where the view won't update itself if modified from this thread
            teacherView.post(new Runnable() {
                @Override
                public void run() {
                    subjectView.setMaxLines(Integer.MAX_VALUE);
                    teacherView.setText(teacher);
                    classroomView.setText(room);
                    eventsView.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void collapse() {
            super.collapse();

            subjectView.setMaxLines(1);
            indicatorView.animate().rotation(0);
            teacherView.setText("...");
            classroomView.setText("");

            eventsView.setVisibility(View.VISIBLE);
        }

        void reset() {
            subjectView.setText("");

            teacherView.setVisibility(View.VISIBLE);
            teacherView.setText("...");

            classroomView.setText("");
            indicatorView.setRotationX(0);
            indicatorView.setVisibility(View.INVISIBLE);

            eventsView.removeAllViews();
            eventsView.setVisibility(View.VISIBLE);

            setTwoLine((ConstraintLayout) subjectView.getParent());
        }
    }

    /**
     * A class to hold all references to each child's child views.
     */
    static class ChildViewHolder extends com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder {
        final TextView subjectView;
        final TextView classroomView;
        final TextView teacherView;
        final View divider;

        ChildViewHolder(View view) {
            super(view);
            subjectView = view.findViewById(R.id.item_subject);
            classroomView = view.findViewById(R.id.item_classroom);
            teacherView = view.findViewById(R.id.item_teacher);
            divider = view.findViewById(R.id.divider);
        }

        void reset() {
            teacherView.setVisibility(View.VISIBLE);
            setTwoLine((ConstraintLayout) subjectView.getParent());
        }
    }

    private static void setSingleLine(ConstraintLayout rootView) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootView);
        constraintSet.connect(R.id.item_subject, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        constraintSet.setVerticalBias(R.id.item_subject, 0.25f);
        constraintSet.applyTo(rootView);
    }

    private static void setTwoLine(ConstraintLayout rootView) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootView);
        constraintSet.connect(R.id.item_subject, ConstraintSet.BOTTOM, R.id.guideline, ConstraintSet.TOP, 0);
        constraintSet.setVerticalBias(R.id.item_subject, 1f);
        constraintSet.applyTo(rootView);
    }
}

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.data.schedule.Lesson;
import com.blackcracks.blich.data.schedule.Period;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import timber.log.Timber;

public class ScheduleAdapter extends ExpandableRecyclerViewAdapter<ScheduleAdapter.GroupViewHolder, ScheduleAdapter.ChildViewHolder> {

    private Context mContext;
    private String mAteKey;

    private int mPrimaryTextColor;
    private int mHourTextColor;

    public ScheduleAdapter(List<? extends ExpandableGroup> groups,
                           Context context) {
        super(groups);

        mContext = context;
        mAteKey = ((MainActivity) mContext).getATEKey();
        mPrimaryTextColor = Config.textColorPrimary(mContext, mAteKey);
        if (mAteKey.equals("light_theme"))
            mHourTextColor = ATEUtil.isColorLight(Config.accentColor(mContext, mAteKey)) ?
                    ContextCompat.getColor(mContext, R.color.text_color_primary_light) :
                    ContextCompat.getColor(mContext, R.color.text_color_primary_dark);
        else
            mHourTextColor = Config.textColorPrimary(mContext, mAteKey);
    }

    public void clear() {
        getGroups().clear();
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_schedule_group, parent, false);
        GroupViewHolder holder = new GroupViewHolder(view);
        return holder;
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder,
                                      int flatPosition,
                                      ExpandableGroup group) {

        holder.reset();
        int groupPosition = expandableList.getUnflattenedPosition(flatPosition).groupPos;

        Period period = (Period) group;
        Lesson firstLesson = period.getFirstLesson();
        boolean isModified = firstLesson.getModifier() != null;

        //The main data
        String subject = firstLesson.buildTitle();
        String teacher;
        String room;
        if (isModified) {
            teacher = "";
            room = "";
        } else {
            teacher = firstLesson.getTeacher();
            room = firstLesson.getRoom();
        }

        int color = firstLesson.getColor();
        if (color == -1)
            color = mPrimaryTextColor;

        holder.hourView.setText(period.getPeriodNum() + "");
        holder.hourView.setTextColor(mHourTextColor);
        if (!mAteKey.equals("light_theme"))
            holder.hourView.setBackground(null);

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);
        holder.setData(teacher, room);

        //Add dots to signify that there are changes
        for(int modifiedColor : period.getChangeTypeColors())
            makeEventDot(holder.eventsView, modifiedColor);

        if (isModified)
            setSingleLine((ConstraintLayout) holder.subjectView.getParent());

        boolean isSingleLesson = group.getItemCount() == 0;

        //Display the correct state of the group
        if (expandableList.expandedGroupIndexes[groupPosition] || isSingleLesson) {
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
            holder.setOnGroupClickListener(new OnGroupClickListener() {
                @Override
                public boolean onGroupClick(int flatPos) {
                    return false;
                }
            });
        } else {
            holder.indicatorView.setVisibility(View.VISIBLE);
            holder.setOnGroupClickListener(new OnGroupClickListener() {
                @Override
                public boolean onGroupClick(int flatPos) {
                    return toggleGroup(flatPos);
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
                                      int flatPosition,
                                      ExpandableGroup group,
                                      int childPosition) {
        holder.reset();
        Lesson lesson = (Lesson) group.getItems().get(childPosition);

        boolean isModified = lesson.getModifier() != null;

        String subject = lesson.buildTitle();
        final String teacher;
        final String room;
        if (isModified) {
            teacher = lesson.getTeacher();
            room = lesson.getRoom();
        } else {
            teacher = lesson.getTeacher();
            room = lesson.getRoom();
        }

        int color = lesson.getColor();
        if (color == -1)
            color = mPrimaryTextColor;

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);

        if (isModified)
            setSingleLine((ConstraintLayout) holder.subjectView.getParent());

        holder.teacherView.setText(teacher);
        holder.classroomView.setText(room);

        //Set a bottom divider if this is the last child
        if (group.getItemCount() - 1 == childPosition) {
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.divider.setVisibility(View.GONE);
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

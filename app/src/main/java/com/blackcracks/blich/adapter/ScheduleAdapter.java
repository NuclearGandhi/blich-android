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

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
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
import com.thoughtbot.expandablerecyclerview.models.IExpandableGroup;

import java.util.List;

public class ScheduleAdapter extends ExpandableRecyclerViewAdapter<ScheduleAdapter.GroupViewHolder, ScheduleAdapter.ChildViewHolder> {

    private Context mContext;
    private String mAteKey;

    private int mPrimaryTextColor;
    private int mHourTextColor;

    public ScheduleAdapter(List<? extends IExpandableGroup> groups,
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

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_schedule_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder,
                                      int flatPosition,
                                      IExpandableGroup group) {
        holder.reset();

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

        boolean isSingleLesson = group.getItemCount() == 0;
        boolean isLastItem = flatPosition == getItemCount() - 1;
        holder.setData(isLastItem, isSingleLesson, teacher, room);

        //Add dots to signify that there are changes
        for (int modifiedColor : period.getChangeTypeColors())
            makeEventDot(holder.eventsView, modifiedColor);

        if (isModified)
            setSingleLine((ConstraintLayout) holder.subjectView.getParent());
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_schedule_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder,
                                      int flatPosition,
                                      IExpandableGroup group,
                                      int childPosition) {
        holder.reset();
        Lesson lesson = (Lesson) group.getItems().get(childPosition);

        boolean isModified = lesson.getModifier() != null;

        String subject = lesson.buildTitle();
        final String teacher;
        final String room;
        if (isModified) {
            teacher = "";
            room = "";
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
        boolean isLastChild = group.getItemCount() - 1 == childPosition;
        boolean isLastItem = flatPosition == getItemCount() - 1;
        if (isLastChild && !isLastItem) {
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
        private final View divider;

        private boolean isLastItem = false;
        private boolean isSingleLesson = false;
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
            divider = view.findViewById(R.id.divider);
        }

        void setData(boolean isLastItem, boolean isSingleLesson, String teacher, String room) {
            this.isLastItem = isLastItem;
            this.isSingleLesson = isSingleLesson;
            this.teacher = teacher;
            this.room = room;
        }

        void refreshDivider(boolean isExpanded) {
            if ((isExpanded && !isSingleLesson) || isLastItem)
                divider.setVisibility(View.GONE);
            else
                divider.setVisibility(View.VISIBLE);
        }

        void refreshSingleState() {
            if (isSingleLesson) {
                teacherView.setText(teacher);
                classroomView.setText(room);
            } else {
                indicatorView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void expand() {
            super.expand();
            indicatorView.animate().rotation(180);

            subjectView.setMaxLines(Integer.MAX_VALUE);
            teacherView.setText(teacher);
            classroomView.setText(room);
            eventsView.setVisibility(View.GONE);

            refreshDivider(true);
            refreshSingleState();
        }

        @Override
        public void collapse() {
            super.collapse();

            if (isSingleLesson) {
                expand();
                return;
            }

            indicatorView.animate().rotation(0);

            subjectView.setMaxLines(1);
            teacherView.setText("...");
            classroomView.setText("");

            eventsView.setVisibility(View.VISIBLE);
            refreshDivider(false);
            refreshSingleState();
        }

        void reset() {
            isLastItem = false;
            isSingleLesson = false;
            subjectView.setText("");

            teacherView.setVisibility(View.VISIBLE);
            teacherView.setText("...");

            classroomView.setText("");
            indicatorView.setRotationX(0);
            indicatorView.setVisibility(View.INVISIBLE);

            eventsView.removeAllViews();
            eventsView.setVisibility(View.VISIBLE);

            divider.setVisibility(View.VISIBLE);

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

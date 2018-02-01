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

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;
import com.blackcracks.blich.util.RealmScheduleHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleAdapter extends BaseExpandableListAdapter {

    private ExpandableListView mExpandableListView;
    private RealmScheduleHelper mRealmScheduleHelper;
    private Context mContext;
    private TextView mStatusTextView;
    private SparseBooleanArray mExpandedArray;

    public ScheduleAdapter(ExpandableListView expandableListView,
                           Context context,
                           TextView statusTextView) {
        mContext = context;
        mStatusTextView = statusTextView;
        mExpandableListView = expandableListView;

        mRealmScheduleHelper = new RealmScheduleHelper(null);
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
        int count = mRealmScheduleHelper.getChildCount(groupPosition);
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

        //Get the overall data object
        Hour hour = (Hour) getGroup(groupPosition);

        //Set the hour indicator text
        holder.hourView.setText(hour.getHour() + "");

        //Get all the lessons and events
        final List<Lesson> lessons = hour.getLessons();

        Lesson firstLesson;
        Event singleEvent = mRealmScheduleHelper.getSingleChildHour(hour);

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
        if (singleEvent != null) { //Then display the event
            subject = singleEvent.buildName();
            teacher = "";
            room = "";
            color = ContextCompat.getColor(mContext, R.color.lesson_event);
        } else {
            firstLesson = lessons.get(0); //We need to display the first lesson in the group collapsed mode
            DatedLesson replacement = mRealmScheduleHelper.getLessonReplacement(firstLesson);


            if (replacement == null) { //Then display a normal lesson
                subject = filterSubject(firstLesson.getSubject());
                teacher = firstLesson.getTeacher();
                room = firstLesson.getRoom();
                color = ContextCompat.getColor(mContext, R.color.black_text);
            } else { //Then display a modified lesson
                subject = replacement.buildName();
                teacher = "";
                room = "";
                color = replacement.getColor(mContext);
            }
        }

        //Set the color according to the lesson type
        holder.subjectView.setTextColor(ContextCompat.getColor(mContext, color));

        //Set initial values
        holder.subjectView.setText(subject);
        holder.teacherView.setText("...");
        holder.classroomView.setText("");
        holder.indicatorView.setRotationX(0);

        holder.eventsView.removeAllViews();
        holder.eventsView.setVisibility(View.VISIBLE);

        //Add dots to signify that there are changes
        if (singleEvent == null) {
            List<DatedLesson> datedLessons = mRealmScheduleHelper.getDatedLessons(hour);
            removeDuplicateDaterLessons(datedLessons);
            loadDatedLessonsToEventDots(holder.eventsView, datedLessons);
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

        Lesson lesson = (Lesson) getChild(groupPosition, childPosition);
        DatedLesson datedLesson;
        if (lesson == null) {//This is not a replacer DatedLesson
            List<DatedLesson> datedLessons = mRealmScheduleHelper.getDatedLessons((Hour) getGroup(groupPosition));
            int lastLessonPos = mRealmScheduleHelper.getLessonCount(groupPosition) - 1;
            datedLesson = datedLessons.get(lastLessonPos - childPosition);
        } else {
            datedLesson = mRealmScheduleHelper.getLessonReplacement(lesson);
        }
        String subject;
        final String teacher;
        final String room;
        int color;

        if (datedLesson != null) {
            subject = datedLesson.buildName();
            teacher = "";
            room = "";
            color = datedLesson.getColor(mContext);
        } else {
            subject = lesson.getSubject();
            teacher = lesson.getTeacher();
            room = lesson.getRoom();
            color = ContextCompat.getColor(mContext, R.color.black_text);
        }

        holder.subjectView.setText(subject);
        holder.subjectView.setTextColor(color);
        holder.teacherView.setText(teacher);
        holder.classroomView.setText(room);

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

    private void removeDuplicateDaterLessons(List<DatedLesson> list) {
        if (list.size() != 0 || list.size() != 1) {
            //Get all the changes, and remove all duplicate types
            //Build the comparator
            Comparator<DatedLesson> typeComparator = new Comparator<DatedLesson>() {
                @Override
                public int compare(DatedLesson o1, DatedLesson o2) {
                    return o1.getType().compareTo(o2.getType());
                }
            };

            //Sort
            Collections.sort(list, typeComparator);

            //Delete
            for (int i = 1; i < list.size(); i++) {
                DatedLesson lesson = list.get(i);
                DatedLesson prevLesson = list.get(i - 1);
                if (lesson.getType().equals(prevLesson.getType())) list.remove(lesson);
            }
        }
    }

    private void loadDatedLessonsToEventDots(ViewGroup parent, List<DatedLesson> list) {
        for (DatedLesson lesson :
                list) {
            makeEventDot(parent, lesson.getColor(mContext));
        }
    }

    private void makeEventDot(ViewGroup parent, @ColorInt int color) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.schedule_event_dot, parent, false);

        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.events_dot);
        drawable.setColor(color);
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

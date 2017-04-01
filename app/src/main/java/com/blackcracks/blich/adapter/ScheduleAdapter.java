package com.blackcracks.blich.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;
import com.blackcracks.blich.fragment.ScheduleDayFragment;

import java.util.Locale;

public class ScheduleAdapter extends CursorAdapter {

    public ScheduleAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.listview_schedule_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        //Get the data from the cursor
        int hour = cursor.getInt(ScheduleDayFragment.COL_HOUR);
        String subjectsValue = cursor.getString(ScheduleDayFragment.COL_SUBJECT);
        String classroomsValue = cursor.getString(ScheduleDayFragment.COL_CLASSROOM);
        String teachersValue = cursor.getString(ScheduleDayFragment.COL_TEACHER);
        String lessonTypesValue = cursor.getString(ScheduleDayFragment.COL_LESSON_TYPE);

        holder.hourView.setText(String.format(Locale.getDefault(), "%d", hour));

        //Handle the the cases of more than one lesson, or missing teacher/classroom
        String[] subjects = subjectsValue.split(";");
        String[] classrooms;
        if (classroomsValue != null) {
            classrooms = classroomsValue.split(";");
        } else {
            classrooms = new String[subjects.length + 1];
        }
        String[] teachers;
        if (teachersValue != null) {
            teachers = teachersValue.split(";");
        } else {
            teachers = new String[subjects.length + 1];
        }

        String[] lessonTypes = lessonTypesValue.split(";");
        holder.infoLinearLayout.removeAllViews();

        for (int i = 0; i < subjects.length; i++) {
            @SuppressLint("InflateParams")
            View info = LayoutInflater.from(context).inflate(
                    R.layout.listview_schedule_info,
                    null);
            InfoViewHolder infoHolder = new InfoViewHolder(info);
            infoHolder.subjectView.setText(subjects[i]);
            infoHolder.teacherView.setText(teachers[i]);
            infoHolder.classroomView.setText(classrooms[i]);

            //Color the background accordingly
            int background = 0;
            switch (lessonTypes[i]) {
                case ScheduleEntry.LESSON_TYPE_CANCELED: {
                    background = ContextCompat.getColor(context, R.color.lesson_canceled);
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_CHANGED: {
                    background = ContextCompat.getColor(context, R.color.lesson_changed);
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_EXAM: {
                    background = ContextCompat.getColor(context, R.color.lesson_exam);
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_EVENT: {
                    background = ContextCompat.getColor(context, R.color.lesson_event);
                    break;
                }
            }

            if (background != 0) {
                infoHolder.subjectView.setTextColor(background);
            }

            holder.infoLinearLayout.addView(info);
        }
    }

    private static class ViewHolder {
        private final TextView hourView;
        private final LinearLayout infoLinearLayout;

        ViewHolder(View view) {
            hourView = (TextView) view.findViewById(R.id.listview_hour_textview);
            infoLinearLayout = (LinearLayout) view.findViewById(R.id.listview_info_linearlayout);
        }
    }

    private static class InfoViewHolder {
        private final TextView subjectView;
        private final TextView classroomView;
        private final TextView teacherView;

        private InfoViewHolder(View view) {
            subjectView = (TextView) view.findViewById(R.id.listview_subject_textview);
            classroomView = (TextView) view.findViewById(R.id.listview_classroom_textview);
            teacherView = (TextView) view.findViewById(R.id.listview_teacher_textview);
        }
    }
}

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

        int hour = cursor.getInt(ScheduleDayFragment.COL_HOUR);
        String subjectsValue = cursor.getString(ScheduleDayFragment.COL_SUBJECT);
        String classroomsValue = cursor.getString(ScheduleDayFragment.COL_CLASSROOM);
        String teachersValue = cursor.getString(ScheduleDayFragment.COL_TEACHER);
        String lessonTypesValue = cursor.getString(ScheduleDayFragment.COL_LESSON_TYPE);

        holder.hourView.setText(String.format(Locale.getDefault(), "%d", hour));

        String[] subjects = subjectsValue.split(",");
        String[] classrooms = classroomsValue.split(",");
        String[] teachers = teachersValue.split(",");
        String[] lessonTypes = lessonTypesValue.split(",");
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
            switch (lessonTypes[i]) {
                case ScheduleEntry.LESSON_TYPE_CANCELED: {
                    info.setBackgroundColor(ContextCompat.getColor(context, R.color.lesson_canceled));
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_CHANGED: {
                    info.setBackgroundColor(ContextCompat.getColor(context, R.color.lesson_changed));
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_EXAM: {
                    info.setBackgroundColor(ContextCompat.getColor(context, R.color.lesson_exam));
                    break;
                }
                case ScheduleEntry.LESSON_TYPE_EVENT: {
                    info.setBackgroundColor(ContextCompat.getColor(context, R.color.lesson_event));
                    break;
                }
            }
            if (i == subjects.length - 1) {
                int padding_in_dp = 16;
                final float scale = context.getResources().getDisplayMetrics().density;
                int padding = (int) (padding_in_dp * scale + 0.5f);
                info.setPaddingRelative(padding, padding, padding, padding);
            }
            holder.infoLinearLayout.addView(info);
        }
    }

    private static class ViewHolder {
        public final TextView hourView;
        public final LinearLayout infoLinearLayout;

        public ViewHolder(View view) {
            hourView = (TextView) view.findViewById(R.id.listview_hour_textview);
            infoLinearLayout = (LinearLayout) view.findViewById(R.id.listview_info_linearlayout);
        }
    }

    private static class InfoViewHolder {
        public final TextView subjectView;
        public final TextView classroomView;
        public final TextView teacherView;

        public InfoViewHolder(View view) {
            subjectView = (TextView) view.findViewById(R.id.listview_subject_textview);
            classroomView = (TextView) view.findViewById(R.id.listview_classroom_textview);
            teacherView = (TextView) view.findViewById(R.id.listview_teacher_textview);
        }
    }
}

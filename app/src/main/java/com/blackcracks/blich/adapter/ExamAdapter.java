package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;

import java.util.Calendar;
import java.util.Locale;

public class ExamAdapter extends CursorAdapter {

    private final Context mContext;

    public ExamAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.exam_item, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String teachers = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_TEACHER));
        long dateInMillis = cursor.getLong(cursor.getColumnIndex(ExamsEntry.COL_DATE));

        if (teachers.equals("wut")) {

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(dateInMillis);

            String month = date.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    new Locale("iw"));
            int year = date.get(Calendar.YEAR);

            TextView monthDivider = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.exam_month_divider, parent);

            String monthText = month + " " + year;
            monthDivider.setText(monthText);
            return monthDivider;
        }

        View view;
        view = newView(mContext, cursor, parent);
        bindView(view, mContext, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String subject = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_SUBJECT));
        long dateInMillis = cursor.getLong(cursor.getColumnIndex(ExamsEntry.COL_DATE));
        String teacher = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_TEACHER));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayOfWeek = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.SHORT,
                new Locale("iw"));

        viewHolder.examDay.setText(String.valueOf(day));
        viewHolder.examDayOfWeek.setText(dayOfWeek);
        viewHolder.examSubject.setText(subject);
        viewHolder.examTeacher.setText(teacher);
    }

    private static class ViewHolder {

        final TextView examDay;
        final TextView examDayOfWeek;
        final TextView examSubject;
        final TextView examTeacher;

        ViewHolder(View itemView) {
            examDay = itemView.findViewById(R.id.exam_day);
            examDayOfWeek = itemView.findViewById(R.id.exam_day_of_week);
            examSubject = itemView.findViewById(R.id.exam_subject);
            examTeacher = itemView.findViewById(R.id.exam_teacher);
        }
    }
}

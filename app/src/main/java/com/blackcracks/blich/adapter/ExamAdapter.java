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
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;
import java.util.Locale;

public class ExamAdapter extends CursorAdapter {

    private final static String LOG_TAG = ExamAdapter.class.getSimpleName();

    private Context mContext;

    public ExamAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
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
        String date = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_DATE));

        if (teachers.equals("wut")) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Utilities.getTimeInMillisFromDate(date));

            String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            int year = calendar.get(Calendar.YEAR);

            TextView monthDivider = (TextView) LayoutInflater.from(mContext)
                    .inflate(R.layout.exam_month_divider, null);

            monthDivider.setText(month + " " + year);
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
        String date = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_DATE));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Utilities.getTimeInMillisFromDate(date));
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK,
                Calendar.SHORT,
                Locale.getDefault());

        viewHolder.examDay.setText(String.valueOf(day));
        viewHolder.examDayOfWeek.setText(dayOfWeek);
        viewHolder.examSubject.setText(subject);
    }

    private static class ViewHolder {

        TextView examDay;
        TextView examDayOfWeek;
        TextView examSubject;

        ViewHolder(View itemView) {
            examDay = (TextView) itemView.findViewById(R.id.exam_day);
            examDayOfWeek = (TextView) itemView.findViewById(R.id.exam_month);
            examSubject = (TextView) itemView.findViewById(R.id.exam_subject);
        }
    }
}

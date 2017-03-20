package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String subject = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_SUBJECT));
        String date = cursor.getString(cursor.getColumnIndex(ExamsEntry.COL_DATE));

        Locale locale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        Date examDate = null;
        try {
            examDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }

        long time = examDate.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

        viewHolder.examDay.setText(String.valueOf(day));
        viewHolder.examMonth.setText(month);
        viewHolder.examSubject.setText(subject);
    }

    private static class ViewHolder {

        TextView examDay;
        TextView examMonth;
        TextView examSubject;

        ViewHolder(View itemView) {
            examDay = (TextView) itemView.findViewById(R.id.exam_day);
            examMonth = (TextView) itemView.findViewById(R.id.exam_month);
            examSubject = (TextView) itemView.findViewById(R.id.exam_subject);
        }
    }
}

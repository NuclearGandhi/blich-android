package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.ExamsEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExamAdapter extends CursorRecyclerViewAdapter<ExamAdapter.ViewHolder> {

    private final static String LOG_TAG = ExamAdapter.class.getSimpleName();

    private Context mContext;

    public ExamAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.exam_item, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(examDate.getTime());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);

        viewHolder.examDay.setText(String.valueOf(day));
        viewHolder.examMonth.setText(month);
        viewHolder.examSubject.setText(subject);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView examDay;
        TextView examMonth;
        TextView examSubject;

        ViewHolder(View itemView) {
            super(itemView);
            examDay = (TextView) itemView.findViewById(R.id.exam_day);
            examMonth = (TextView) itemView.findViewById(R.id.exam_month);
            examSubject = (TextView) itemView.findViewById(R.id.exam_subject);
        }
    }
}

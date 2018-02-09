package com.blackcracks.blich.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.ExamItem;
import com.blackcracks.blich.data.GenericExam;
import com.blackcracks.blich.data.MonthDivider;
import com.blackcracks.blich.util.RealmExamHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExamAdapter extends BaseAdapter {

    private final Context mContext;
    private RealmExamHelper mExamHelper;
    private TextView mStatusMessage;

    public ExamAdapter(Context context, List<Exam> data, TextView statusMessage) {
        mContext = context;
        mExamHelper = new RealmExamHelper(data);
        mStatusMessage = statusMessage;
    }

    public void switchData(List<Exam> data) {
        mExamHelper.switchData(data);
    }

    @Override
    public int getCount() {
        int count = mExamHelper.getCount();
        if (count == 0) mStatusMessage.setVisibility(View.VISIBLE);
        else mStatusMessage.setVisibility(View.INVISIBLE);

        return count;
    }

    @Override
    public Object getItem(int position) {
        return mExamHelper.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExamItem item = (ExamItem) getItem(position);
        View view = null;
        switch (item.getType()) {
            case ExamItem.TYPE_EXAM: {
                GenericExam exam = (GenericExam) item;
                if (convertView == null || convertView instanceof TextView) {
                    view = newView(parent);
                } else {
                    view = convertView;
                }
                bindView(view, exam);
                break;
            }
            case ExamItem.TYPE_MONTH:{
                MonthDivider divider = (MonthDivider) item;
                view = buildMonthDivider(parent, divider);
            }
        }
        return view;
    }

    private View newView(ViewGroup parent) {
        View view =
                LayoutInflater.from(mContext).inflate(R.layout.exam_item, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    private void bindView(View view, GenericExam exam) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String subject = exam.getName();
        long dateInMillis = exam.getDate().getTime();
        String teacher = exam.buildTeacherString();

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

    private View buildMonthDivider(ViewGroup parent, MonthDivider divider) {
        TextView view = (TextView) LayoutInflater.from(mContext).inflate(
                R.layout.exam_month_divider, parent, false);
        view.setText(divider.buildLabel());

        return view;
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
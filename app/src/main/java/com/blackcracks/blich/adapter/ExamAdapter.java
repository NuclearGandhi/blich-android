/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

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

/**
 * {@link android.widget.Adapter} having two views: month dividers and exams.
 */
public class ExamAdapter extends BaseAdapter {

    private final Context mContext;
    private RealmExamHelper mExamHelper;
    private TextView mStatusMessage;

    /**
     * @param data exams to be displayed.
     * @param statusMessage {@link TextView} for when the data is invalid.
     */
    @SuppressWarnings("SameParameterValue")
    public ExamAdapter(Context context, List<Exam> data, TextView statusMessage) {
        mContext = context;
        mExamHelper = new RealmExamHelper(data);
        mStatusMessage = statusMessage;
    }

    /**
     * Switch the exams to be displayed. Calls {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param data exams to switch to.
     */
    public void switchData(List<Exam> data) {
        mExamHelper.switchData(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int count = mExamHelper.getCount();
        //Set the status TextView according to the validity of the data.
        if (count == 0) mStatusMessage.setVisibility(View.VISIBLE);
        else mStatusMessage.setVisibility(View.GONE);

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

        //Check the type of the item
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

        //Get readable date from epoch in millis.
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

    /**
     * A class to hold all references to each exam item's child views.
     */
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
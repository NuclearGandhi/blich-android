/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.ExamItem;
import com.blackcracks.blich.data.GenericExam;
import com.blackcracks.blich.data.MonthDivider;
import com.blackcracks.blich.adapter.helper.RealmExamHelper;
import com.blackcracks.blich.dialog.ExamReminderDialog;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * {@link android.widget.Adapter} having two views: month dividers and exams.
 */
public class ExamAdapter extends BaseAdapter {

    private static final String DIALOG_TAG_EXAM_REMINDER = "exam_reminder";

    private final Context mContext;
    private FragmentManager mFragmentManager;

    private float mEnabledButtonAlpha;
    private float mDisabledButtonAlpha;
    private int mMonthDividerTextColor;

    private RealmExamHelper mExamHelper;
    private TextView mStatusMessage;

    /**
     * @param data exams to be displayed.
     * @param statusMessage {@link TextView} for when the data is invalid.
     */
    @SuppressWarnings("SameParameterValue")
    public ExamAdapter(
            Context context,
            FragmentManager fragmentManager,
            List<Exam> data,
            TextView statusMessage) {
        mContext = context;
        mFragmentManager = fragmentManager;
        mExamHelper = new RealmExamHelper(data);
        mStatusMessage = statusMessage;

        String ateKey = Utilities.getATEKey(context);

        boolean isPrimaryDarkColorLight = ATEUtil.isColorLight(
                Config.primaryColorDark(context, ateKey));
        mMonthDividerTextColor = isPrimaryDarkColorLight ?
                ContextCompat.getColor(context, R.color.text_color_primary_light) :
                ContextCompat.getColor(context, R.color.text_color_primary_dark);

        if (ateKey.equals("light_theme")){
            mEnabledButtonAlpha = 0.87f;
            mDisabledButtonAlpha = 0.54f;
        }
        else {
            mEnabledButtonAlpha = 1f;
            mDisabledButtonAlpha = 0.7f;
        }
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
                LayoutInflater.from(mContext).inflate(R.layout.item_exam, parent, false);

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
        viewHolder.notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExamReminderDialog()
                        .show(mFragmentManager, DIALOG_TAG_EXAM_REMINDER);
            }
        });
        viewHolder.notificationButton.setAlpha(mDisabledButtonAlpha);
    }

    private View buildMonthDivider(ViewGroup parent, MonthDivider divider) {
        TextView view = (TextView) LayoutInflater.from(mContext).inflate(
                R.layout.exam_month_divider, parent, false);
        view.setText(divider.buildLabel());
        view.setTextColor(mMonthDividerTextColor);

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
        final ImageButton notificationButton;

        ViewHolder(View itemView) {
            examDay = itemView.findViewById(R.id.item_day_in_month);
            examDayOfWeek = itemView.findViewById(R.id.item_day_of_week);
            examSubject = itemView.findViewById(R.id.item_subject);
            examTeacher = itemView.findViewById(R.id.item_teacher);
            notificationButton = itemView.findViewById(R.id.item_notification_button);
        }
    }
}
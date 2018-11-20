/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.R;
import com.blackcracks.blich.adapter.helper.RealmExamHelper;
import com.blackcracks.blich.data.exam.Exam;
import com.blackcracks.blich.data.exam.ExamItem;
import com.blackcracks.blich.data.exam.MonthDivider;
import com.blackcracks.blich.util.Utilities;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * {@link android.widget.Adapter} having two views: month dividers and exams.
 */
public class ExamAdapter extends RecyclerView.Adapter {

    private final Context mContext;

    private int mMonthDividerTextColor;

    private RealmExamHelper mExamHelper;

    /**
     * @param data          exams to be displayed.
     */
    @SuppressWarnings("SameParameterValue")
    public ExamAdapter(
            Context context,
            List<Exam> data) {
        mContext = context;
        mExamHelper = new RealmExamHelper(data);

        String ateKey = Utilities.getATEKey(context);

        boolean isPrimaryDarkColorLight = ATEUtil.isColorLight(
                Config.primaryColorDark(context, ateKey));
        mMonthDividerTextColor = isPrimaryDarkColorLight ?
                ContextCompat.getColor(context, R.color.text_color_primary_light) :
                ContextCompat.getColor(context, R.color.text_color_primary_dark);
    }

    /**
     * Switch the exams to be displayed. Calls {@link BaseAdapter#notifyDataSetChanged()}.
     *
     * @param data exams to switch to.
     */
    public void setData(List<Exam> data) {
        mExamHelper.setData(data);
        notifyDataSetChanged();
    }

    public int getDateFlatPosition(CalendarDay date) {
        for(int i = 0; i < mExamHelper.getCount(); i++) {
            ExamItem item = mExamHelper.getItem(i);
            if (item instanceof Exam) {
                Exam exam = (Exam) item;
                CalendarDay day = CalendarDay.from(exam.getDate());
                if (date.equals(day))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mExamHelper.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mExamHelper.getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case ExamItem.TYPE_EXAM: {
                return new ExamViewHolder(
                        inflater.inflate(R.layout.item_exam, parent, false));
            }
            case ExamItem.TYPE_MONTH: {
                return new DividerViewHolder(
                        inflater.inflate(R.layout.item_month_divider, parent, false));
            }
            default:
                throw new IllegalArgumentException("Unrecognizable view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ExamItem.TYPE_EXAM: {
                bindExamView(
                        (ExamViewHolder) holder,
                        (Exam) mExamHelper.getItem(position));
                break;
            }
            case ExamItem.TYPE_MONTH: {
                bindDividerView(
                        (DividerViewHolder) holder,
                        (MonthDivider) mExamHelper.getItem(position));
                break;
            }
            default:
                throw new IllegalArgumentException("Unrecognizable view type: " + getItemViewType(position));
        }
    }

    private void bindExamView(ExamViewHolder holder, Exam exam) {
        String subject = exam.getBaseTitle();
        long dateInMillis = exam.getDate().getTime();
        String teachers = exam.buildTeachersString();

        //Get readable date from epoch in millis.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayOfWeek = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.SHORT,
                new Locale("iw"));

        holder.examDay.setText(String.valueOf(day));
        holder.examDayOfWeek.setText(dayOfWeek);
        holder.examSubject.setText(subject);
        holder.examTeacher.setText(teachers);
    }

    private void bindDividerView(DividerViewHolder holder, MonthDivider divider) {
        holder.month.setText(divider.buildLabel());
        holder.month.setTextColor(mMonthDividerTextColor);
    }

    /**
     * A class to hold all references to each exam item's child views.
     */
    private static class ExamViewHolder extends RecyclerView.ViewHolder {
        final TextView examDay;
        final TextView examDayOfWeek;
        final TextView examSubject;
        final TextView examTeacher;

        ExamViewHolder(View itemView) {
            super(itemView);
            examDay = itemView.findViewById(R.id.item_day_in_month);
            examDayOfWeek = itemView.findViewById(R.id.item_day_of_week);
            examSubject = itemView.findViewById(R.id.item_subject);
            examTeacher = itemView.findViewById(R.id.item_teacher);
        }
    }

    private static class DividerViewHolder extends RecyclerView.ViewHolder {
        final TextView month;

        public DividerViewHolder(View itemView) {
            super(itemView);
            month = itemView.findViewById(R.id.item_month);
        }
    }
}
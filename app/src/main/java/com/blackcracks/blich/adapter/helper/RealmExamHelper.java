/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter.helper;

import com.blackcracks.blich.data.exam.ExamItem;
import com.blackcracks.blich.data.exam.Exam;
import com.blackcracks.blich.data.exam.MonthDivider;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * A helper class to easily extract information from given data.
 */
public class RealmExamHelper {

    private List<ExamItem> mExamItems = new ArrayList<>();
    private boolean mIsDataValid;

    public RealmExamHelper(List<Exam> exams) {
        setData(exams);
    }

    /**
     * Switch to the given data
     *
     * @param exams data to switch to.
     */
    public void setData(List<Exam> exams) {
        //Check if the data is valid
        try {
            mIsDataValid = exams != null && !exams.isEmpty();
        } catch (IllegalStateException e) { //In case Realm instance has been closed
            mIsDataValid = false;
            Timber.d("Realm has been closed");
        }

        if (mIsDataValid) {
            mExamItems.clear();
            buildMonthDividers(exams);
        }
    }

    public boolean isDataValid() {
        return mIsDataValid;
    }

    /**
     * Add month dividers to the data list.
     *
     * @param exams the list of {@link Exam}s.
     */
    private void buildMonthDividers(List<Exam> exams) {
        mExamItems.add(
                new MonthDivider(exams.get(0).getDate()));
        mExamItems.add(exams.get(0));

        for (int i = 1; i < exams.size(); i++) {
            Exam exam = exams.get(i);
            if (!exam.equalsByMonth(exams.get(i - 1))) {
                mExamItems.add(
                        new MonthDivider(exam.getDate()));
            }

            mExamItems.add(exam);
        }
    }

    /**
     * Get the item in the given position.
     *
     * @param position position of wanted item.
     * @return an {@link ExamItem}.
     */
    public ExamItem getItem(int position) {
        if (!mIsDataValid)
            return null;
        return mExamItems.get(position);
    }

    /**
     * Get the count of items.
     *
     * @return item count.
     */
    public int getCount() {
        if (mIsDataValid) return mExamItems.size();
        else return 0;
    }
}

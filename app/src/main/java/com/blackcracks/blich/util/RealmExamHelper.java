/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.ExamItem;
import com.blackcracks.blich.data.GenericExam;
import com.blackcracks.blich.data.MonthDivider;

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
        switchData(exams);
    }

    /**
     * Switch to the given data
     *
     * @param exams data to switch to.
     */
    public void switchData(List<Exam> exams) {
        //Check if the data is valid
        try {
            mIsDataValid = exams != null && !exams.isEmpty();
        } catch (IllegalStateException e) { //In case Realm instance has been closed
            mIsDataValid = false;
            Timber.d("Realm has been closed");
        }

        if (mIsDataValid) {
            mExamItems.clear();
            List<GenericExam> genericExams = GenericExam.buildExamsList(exams);
            buildMonthDividers(genericExams);
        }
    }

    public boolean isDataValid() {
        return mIsDataValid;
    }

    /**
     * Add month dividers to the data list.
     *
     * @param exams the list of {@link GenericExam}s.
     */
    private void buildMonthDividers(List<GenericExam> exams) {
        mExamItems.add(
                new MonthDivider(exams.get(0).getDate()));

        for (int i = 1; i < exams.size(); i++) {
            GenericExam exam = exams.get(i);
            if (!exam.equalToByMonth(exams.get(i - 1))) {
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

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

public class RealmExamHelper {

    private List<ExamItem> mExamItems = new ArrayList<>();
    private List<GenericExam> mExams = new ArrayList<>();
    private boolean mIsDataValid;

    public RealmExamHelper(List<Exam> exams) {
        switchData(exams);
    }

    public void switchData(List<Exam> exams) {
        mIsDataValid = exams != null && exams.size() != 0;
        if (mIsDataValid) {
            mExams.clear();
            mExamItems.clear();
            buildExamsList(exams);
            buildMonthDividers();
        }
    }

    public boolean isDataValid() {
        return mIsDataValid;
    }

    private void buildExamsList(List<Exam> exams) {
        GenericExam genericExam = null;
        for (Exam exam :
                exams) {
            if (genericExam == null) genericExam = new GenericExam(exam);
            else {
                boolean didAdd = genericExam.addExam(exam);
                if (!didAdd) {
                    mExams.add(genericExam);
                    genericExam = null;
                }
            }
        }
    }

    private void buildMonthDividers() {
        mExamItems.add(
                new MonthDivider(mExams.get(0).getDate()));

        for (int i = 1; i < mExams.size(); i++) {
            GenericExam exam = mExams.get(i);
            if (!exam.equalToByMonth(mExams.get(i - 1))) {
                mExamItems.add(
                        new MonthDivider(exam.getDate()));
            }

            mExamItems.add(exam);
        }
    }

    public int getCount() {
        if (mIsDataValid) return mExamItems.size();
        else return 0;
    }
}

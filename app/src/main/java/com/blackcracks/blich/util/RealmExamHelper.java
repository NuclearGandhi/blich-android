/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.data.GenericExam;

import java.util.ArrayList;
import java.util.List;

public class RealmExamHelper {

    private List<GenericExam> mExams = new ArrayList<>();
    private boolean mIsDataValid;

    public RealmExamHelper(List<Exam> exams) {
        switchData(exams);
    }

    public void switchData(List<Exam> exams) {
        mIsDataValid = exams != null && exams.size() != 0;
        if (mIsDataValid) {
            mExams.clear();

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
    }

    public boolean isDataValid() {
        return mIsDataValid;
    }
}

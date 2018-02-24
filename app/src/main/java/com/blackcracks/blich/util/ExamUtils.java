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

/**
 * A class containing utility methods for exams.
 */
public class ExamUtils {
    /**
     * Convert a {@link List < Exam >} to a {@link List< GenericExam >}.
     *
     * @param exams Exams to convert.
     * @return built GenericExams.
     */
    public static List<GenericExam> buildExamsList(List<Exam> exams) {
        List<GenericExam> genericExams = new ArrayList<>();
        GenericExam genericExam = null;
        for (Exam exam :
                exams) {
            if (genericExam == null) genericExam = new GenericExam(exam);
            else {
                boolean didAdd = genericExam.addExam(exam);
                if (!didAdd) {
                    genericExams.add(genericExam);
                    genericExam = null;
                }
            }
        }

        return genericExams;
    }
}

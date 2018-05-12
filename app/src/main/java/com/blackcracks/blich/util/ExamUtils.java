/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.util;

import com.blackcracks.blich.data.raw.Exam;
import com.blackcracks.blich.data.exam.GenericExam;

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
        List<GenericExam> toReturn = new ArrayList<>();
        for(Exam exam : exams) {
            boolean didAdd = false;
            for (int i = 0; i < toReturn.size() && !didAdd; i++) {
                didAdd = toReturn.get(i).addExam(exam);
            }
            if (!didAdd) toReturn.add(new GenericExam(exam));
        }

        return toReturn;
    }
}

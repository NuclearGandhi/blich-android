/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericExam {

    private Date date;
    private String name;
    private List<String> teachers;

    public GenericExam() {
        teachers = new ArrayList<>();
    }

    public GenericExam(Exam exam) {
        date = exam.getDate();
        name = exam.getName();
        teachers = new ArrayList<>();
        teachers.add(exam.getTeacher());
    }

    public boolean addExam(Exam exam) {
        if (date.equals(exam.getDate()) &&
                name.equals(exam.getName())) {
            teachers.add(exam.getTeacher());
            return true;
        }
        return false;
    }

    public String buildTeacherString() {
        StringBuilder stringBuilder = new StringBuilder(teachers.get(0));
        for (int i = 1; i < teachers.size(); i++) {
            stringBuilder.append(teachers.get(i));
        }
        return stringBuilder.toString();
    }
}

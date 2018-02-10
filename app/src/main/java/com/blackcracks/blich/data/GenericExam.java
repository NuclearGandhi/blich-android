/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GenericExam implements ExamItem {

    private Date date;
    private String name;
    private List<String> teachers;

    public GenericExam() {
        teachers = new ArrayList<>();
    }

    public GenericExam(Exam exam) {}

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<String> teachers) {
        this.teachers = teachers;
    }

    public boolean addExam(Exam exam) {
        if (date.equals(exam.getDate()) && name.equals(exam.getName())) {
            String teacher = exam.getTeacher();
            if (!teachers.contains(teacher)) teachers.add(teacher);
            return true;
        }
        return false;
    }

    public String buildTeacherString() {
        StringBuilder stringBuilder = new StringBuilder(teachers.get(0));
        for (int i = 1; i < teachers.size(); i++) {
            stringBuilder.append(", ").append(teachers.get(i));
        }
        return stringBuilder.toString();
    }

    public boolean equalToByMonth(GenericExam e) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month1 = calendar.get(Calendar.MONTH);
        calendar.setTime(e.getDate());
        int month2 = calendar.get(Calendar.MONTH);
        return month1 == month2;
    }

    @Override
    public int getType() {
        return ExamItem.TYPE_EXAM;
    }
}

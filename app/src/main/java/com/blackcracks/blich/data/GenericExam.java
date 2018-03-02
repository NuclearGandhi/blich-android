/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A class to pack several similar {@link Exam} together.
 */
public class GenericExam implements ExamItem {

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

    /**
     * If possible, add exam's teacher to the list.
     *
     * @param exam {@link Exam} to add.
     * @return {@code true} the {@link Exam} has been added.
     */
    public boolean addExam(Exam exam) {
        if (date.equals(exam.getDate()) && name.equals(exam.getName())) {
            String teacher = exam.getTeacher();
            if (!teachers.contains(teacher)) teachers.add(teacher);
            return true;
        }
        return false;
    }

    /**
     * Build a string containing all the teachers separated by commas.
     *
     * @return a {@link String}.
     */
    public String buildTeacherString() {
        StringBuilder stringBuilder = new StringBuilder(teachers.get(0));
        for (int i = 1; i < teachers.size(); i++) {
            stringBuilder.append(", ").append(teachers.get(i));
        }
        return stringBuilder.toString();
    }

    /**
     * Check if both exams' month are equal.
     *
     * @param e other {@link GenericExam} to compare to.
     * @return {@code true} the months' are equal.
     */
    public boolean equalToByMonth(GenericExam e) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month1 = calendar.get(Calendar.MONTH);
        calendar.setTime(e.getDate());
        int month2 = calendar.get(Calendar.MONTH);
        return month1 == month2;
    }

    @Override
    public @Type int getType() {
        return ExamItem.TYPE_EXAM;
    }
}

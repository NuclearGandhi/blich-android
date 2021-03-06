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

package com.blackcracks.blich.data.exam;

import com.blackcracks.blich.data.raw.RawExam;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * A class to pack several similar {@link RawExam} together.
 */
public class Exam extends RealmObject implements ExamItem {

    private Date date;
    private String baseTitle;
    private String title;

    private int beginPeriod;
    private int endPeriod;
    private RealmList<String> teachers;

    public Exam() {
        teachers = new RealmList<>();
    }

    public Exam(RawExam rawExam) {
        date = rawExam.getDate();
        baseTitle = rawExam.getBaseTitle();
        title = rawExam.buildTitle();

        beginPeriod = rawExam.getBeginHour();
        endPeriod = rawExam.getEndHour();

        teachers = new RealmList<>();
        teachers.add(rawExam.getOldTeacher());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBaseTitle() {
        return baseTitle;
    }

    public void setBaseTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    public int getBeginPeriod() {
        return beginPeriod;
    }

    public void setBeginPeriod(int beginPeriod) {
        this.beginPeriod = beginPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(int endPeriod) {
        this.endPeriod = endPeriod;
    }

    /**
     * If possible, add rawExam's teacher to the list.
     *
     * @param rawExam {@link RawExam} to add.
     * @return {@code true} the {@link RawExam} has been added.
     */
    public boolean addExam(RawExam rawExam) {
        if (equals(rawExam)) {
            String teacher = rawExam.getOldTeacher();
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
    public String buildTeachersString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < teachers.size(); i++) {
            if (teachers.get(i) != null)
                stringBuilder.append(", ").append(teachers.get(i));
        }
        stringBuilder.delete(0, 2);
        return stringBuilder.toString();
    }

    /**
     * Check if both exams' month are equal.
     *
     * @param e other {@link Exam} to compare to.
     * @return {@code true} the months' are equal.
     */
    public boolean equalsByMonth(Exam e) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month1 = calendar.get(Calendar.MONTH);
        calendar.setTime(e.getDate());
        int month2 = calendar.get(Calendar.MONTH);
        return month1 == month2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exam) {
            Exam exam = (Exam) obj;
            return date.equals(exam.getDate()) &&
                    title.equals(exam.getTitle()) &&
                    beginPeriod == exam.getBeginPeriod() &&
                    endPeriod == exam.getEndPeriod();
        }
        return false;
    }

    public boolean equals(RawExam rawExam) {
        return date.equals(rawExam.getDate()) &&
                baseTitle.equals(rawExam.getBaseTitle());
    }

    @Override
    public @ExamItemType
    int getType() {
        return ExamItem.TYPE_EXAM;
    }
}

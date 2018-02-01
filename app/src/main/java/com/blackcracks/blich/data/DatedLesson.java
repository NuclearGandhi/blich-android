/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.annotation.ColorInt;

import java.util.Date;

import io.realm.RealmObject;

public abstract class DatedLesson extends RealmObject implements Comparable<DatedLesson>{

    private Date date;
    private String subject;
    private String teacher;

    public DatedLesson() {}

    public DatedLesson(Date date, String subject, String teacher) {
        setDate(date);
        setSubject(subject);
        setTeacher(teacher);
    }

    public abstract String buildName();
    public abstract @ColorInt int getColor(Context context);
    public abstract boolean isEqualToHour(int hour);

    public boolean isReplacing() {
        return !getTeacher().equals("") && !getSubject().equals("");
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
}

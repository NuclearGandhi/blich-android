/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants;

import java.util.Date;

import io.realm.RealmObject;

public class Exam extends RealmObject implements DatedLesson {

    private String name;
    private Date date;

    private int beginHour;
    private int endHour;

    private String subject;
    private String teacher;

    private String room;

    public Exam() {}

    public Exam(String name, Date date, int beginHour, int endHour, String room) {
        setName(name);
        setDate(date);
        setBeginHour(beginHour);
        setEndHour(endHour);
        setRoom(room);
    }

    @Override
    public String buildName() {
        return getName() + " לקבוצה של " + getTeacher() + ", בחדר " + getRoom();
    }

    @Override
    public String getType() {
        return Constants.Database.TYPE_EXAM;
    }

    @Override
    public int getColor(Context context) {
        return ContextCompat.getColor(context, R.color.lesson_exam);
    }

    @Override
    public boolean isEqualToHour(int hour) {
        return beginHour <= hour && hour <= endHour;
    }

    @Override
    public boolean isAReplacer() {
        return !getTeacher().equals("") && !getSubject().equals("");
    }

    @Override
    public boolean canReplaceLesson(Lesson toReplace) {
        return getTeacher().equals(toReplace.getTeacher()) && getSubject().equals(toReplace.getSubject());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exam) {
            Exam e = (Exam) obj;
            return buildName().equals(e.buildName());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.contains("מבחן") ||
                name.contains("בוחן") ||
                name.contains("מבחני")) {
            this.name = name;
        } else {
            this.name = "מבחן ב" + name;
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        if (beginHour == 0) beginHour++;
        this.beginHour = beginHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}

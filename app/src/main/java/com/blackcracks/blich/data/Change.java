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
import com.blackcracks.blich.util.Constants.Database;

import java.util.Date;

import io.realm.RealmObject;

public class Change extends RealmObject implements DatedLesson, Cloneable {

    private String changeType;
    private Date date;
    private int hour;

    private String subject;
    private String teacher;

    private String newTeacher;
    private String newRoom;
    private int newHour;

    public Change() {
    }

    public Change(Date date, String subject, String teacher, String changeType, int hour) {
        setDate(date);
        setSubject(subject);
        setTeacher(teacher);
        setChangeType(changeType);
        setHour(hour);
    }

    @Override
    public String buildName() {
        String str = "";
        switch (getChangeType()) {
            case Database.TYPE_CANCELED: {
                str = "ביטול " + buildLessonName();
                break;
            }
            case Database.TYPE_NEW_HOUR: {
                str = "הזזת " + buildLessonName() + " לשעה " + getNewHour();
                break;
            }
            case Database.TYPE_NEW_ROOM: {
                str = buildLessonName() + " -> חדר: " + getNewRoom();
                break;
            }
            case Database.TYPE_NEW_TEACHER: {
                str = buildLessonName() + " -> מורה: " + getNewTeacher();
                break;
            }
        }

        return str;
    }

    @Override
    public String getType() {
        return changeType;
    }

    @Override
    public int getColor(Context context) {
        int colorId;
        if (changeType.equals(Database.TYPE_CANCELED)) colorId = R.color.lesson_canceled;
        else colorId = R.color.lesson_changed;

        return ContextCompat.getColor(context, colorId);
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
    public boolean isEqualToHour(int hour) {
        return this.hour == hour;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Change) {
            Change c = (Change) obj;
            return buildName().equals(c.buildName());
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object clone = super.clone();
        Change change = (Change) clone;
        change.setDate(date);
        change.setChangeType(changeType);
        change.setHour(hour);
        change.setSubject(subject);
        change.setTeacher(teacher);
        change.setNewRoom(newRoom);
        change.setNewHour(newHour);
        change.setNewTeacher(newTeacher);

        return change;
    }

    private String buildLessonName() {
        return getSubject() + ", " + getTeacher();
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
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

    public String getNewTeacher() {
        return newTeacher;
    }

    public void setNewTeacher(String newTeacher) {
        this.newTeacher = newTeacher;
    }

    public String getNewRoom() {
        return newRoom;
    }

    public void setNewRoom(String newRoom) {
        this.newRoom = newRoom;
    }

    public int getNewHour() {
        return newHour;
    }

    public void setNewHour(int newHour) {
        this.newHour = newHour;
    }
}

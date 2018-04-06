/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants.Database;
import com.blackcracks.blich.util.PreferenceUtils;

import java.util.Date;

import io.realm.RealmObject;

/**
 * A data class holding information about a change in schedule.
 */
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
        switch (getChangeType()) {
            case Database.TYPE_CANCELED: {
                return  "ביטול " + buildLessonName();
            }
            case Database.TYPE_NEW_HOUR: {
                return  "הזזת " + buildLessonName() + " לשעה " + getNewHour();
            }
            case Database.TYPE_NEW_ROOM: {
                return  buildLessonName() + " -> חדר: " + getNewRoom();
            }
            case Database.TYPE_NEW_TEACHER: {
                return  buildLessonName() + " -> מורה: " + getNewTeacher();
            }
            default: {
                return  buildLessonName();
            }
        }
    }

    @Override
    public String getType() {
        return changeType;
    }

    @Override
    public int getColor() {
        if (changeType.equals(Database.TYPE_CANCELED)) {
            return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_canceled_key);
        } else {
            return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_changed_key);
        }
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

    @Override
    public int getBeginHour() {
        return getHour();
    }

    @Override
    public int getEndHour() {
        return getHour();
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

    private int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    private String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    private String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    private String getNewTeacher() {
        return newTeacher;
    }

    public void setNewTeacher(String newTeacher) {
        this.newTeacher = newTeacher;
    }

    private String getNewRoom() {
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

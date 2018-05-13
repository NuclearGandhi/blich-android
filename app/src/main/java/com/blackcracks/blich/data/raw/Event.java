/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.schedule.DatedLesson;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.PreferenceUtils;

import java.util.Date;

import io.realm.RealmObject;

/**
 * A data class holding information about events.
 */
public class Event extends RealmObject implements DatedLesson {

    private String name;
    private Date date;
    private int beginHour;
    private int endHour;

    private String subject;
    private String teacher;
    private String room;

    public Event() {}

    public Event(Date date, String name, int beginHour, int endHour) {
        setDate(date);
        setName(name);
        setBeginHour(beginHour);
        setEndHour(endHour);
    }

    @Override
    public String buildName() {
        if (room.equals("")) return name;

        return name + ", " + room;
    }

    @Override
    public String getType() {
        return Constants.Database.TYPE_EVENT;
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
    public boolean canReplaceLesson(RawLesson toReplace) {
        return getTeacher().equals(toReplace.getTeacher()) && getSubject().equals(toReplace.getSubject());
    }

    @Override
    public int getColor() {
        return PreferenceUtils.getInstance().getInt(R.string.pref_theme_lesson_event_key);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            Event e = (Event) obj;
            return buildName().equals(e.buildName());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}

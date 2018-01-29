/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.Date;

import io.realm.RealmObject;

public class Exam extends RealmObject {

    private String name;
    private Date date;

    private int beginHour;
    private int endHour;
    private String room;

    private String teacher;
    private String subject;

    public Exam() {}

    public Exam(String name, Date date, int beginHour, int endHour, String room) {
        setName(name);
        setDate(date);
        setBeginHour(beginHour);
        setEndHour(endHour);
        setRoom(room);
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

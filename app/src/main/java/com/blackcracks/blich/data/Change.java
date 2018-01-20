/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import io.realm.RealmObject;

public class Change extends RealmObject {

    private String changeType;

    private int hour;
    private String teacher;
    private String subject;

    private String newTeacher;
    private String newRoom;
    private int newHour;

    public Change() {
    }

    public Change(String changeType, int hour, String teacher, String subject) {
        this.changeType = changeType;
        this.hour = hour;
        this.teacher = teacher;
        this.subject = subject;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
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

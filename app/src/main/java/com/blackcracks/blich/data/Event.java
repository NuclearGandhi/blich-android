/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.Date;

import io.realm.RealmObject;

public class Event extends RealmObject {

    private Date date;
    private String name;
    private int beginHour;
    private int endHour;

    private String room;

    public Event() {}

    public Event(Date date, String name, int beginHour, int endHour) {
        this.date = date;
        this.name = name;
        this.beginHour = beginHour;
        this.endHour = endHour;
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

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
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

    public String buildName() {
        return name + ", " + room;
    }
}

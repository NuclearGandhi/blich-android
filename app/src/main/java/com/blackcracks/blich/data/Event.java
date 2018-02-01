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

import java.util.Date;

public class Event extends DatedLesson {

    private String name;
    private int beginHour;
    private int endHour;

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
    public int getColor(Context context) {
        return ContextCompat.getColor(context, R.color.lesson_event);
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
}

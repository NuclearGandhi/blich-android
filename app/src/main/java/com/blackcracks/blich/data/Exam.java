/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.blackcracks.blich.R;

import java.util.Date;

public class Exam extends DatedLesson {

    private String name;

    private int beginHour;
    private int endHour;
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
    public int getColor(Context context) {
        return ContextCompat.getColor(context, R.color.lesson_exam);
    }

    @Override
    public boolean isEqualToHour(int hour) {
        return beginHour < hour && hour < endHour;
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

    @Override
    public int compareTo(@NonNull DatedLesson o) {
        return 0;
    }
}

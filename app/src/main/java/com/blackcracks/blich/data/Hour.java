/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Hour extends RealmObject {

    private int day;
    private int hour;
    private RealmList<Lesson> lessons;

    public Hour() {

    }

    public Hour(int day, int hour, RealmList<Lesson> lessons) {
        this.day = day;
        this.hour = hour;
        this.lessons = lessons;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(RealmList<Lesson> lessons) {
        this.lessons = lessons;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}

/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Hour extends RealmObject implements Comparable<Hour> {

    private int day;
    private int hour;
    private RealmList<Lesson> lessons;

    @Ignore private List<Event> events = new ArrayList<>();
    @Ignore private List<Exam> exams = new ArrayList<>();

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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public int compareTo(@NonNull Hour o) {
        if (getHour() > o.getHour()) return 1;
        else if (getHour() == o.getHour()) return 0;
        else return -1;
    }
}

/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * A data class holding information about a single hour in the schedule.
 */
public class Hour extends RealmObject implements Comparable<Hour> {

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

    @Override
    public int compareTo(@NonNull Hour o) {
        if (getHour() > o.getHour()) return 1;
        else if (getHour() == o.getHour()) return 0;
        else return -1;
    }
}

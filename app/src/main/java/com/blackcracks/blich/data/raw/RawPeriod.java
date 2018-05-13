/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.raw;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * A data class holding information about a single hour in the schedule.
 */
public class RawPeriod extends RealmObject implements Comparable<RawPeriod> {

    private int day;
    private int hour;
    private RealmList<RawLesson> mRawLessons;

    public RawPeriod() {

    }

    public RawPeriod(int day, int hour, RealmList<RawLesson> rawLessons) {
        this.day = day;
        this.hour = hour;
        this.mRawLessons = rawLessons;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public List<RawLesson> getRawLessons() {
        return mRawLessons;
    }

    public void setRawLessons(RealmList<RawLesson> rawLessons) {
        this.mRawLessons = rawLessons;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public int compareTo(@NonNull RawPeriod o) {
        if (getHour() > o.getHour()) return 1;
        else if (getHour() == o.getHour()) return 0;
        else return -1;
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.data.raw;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * A data class holding information about a single hour in the schedule.
 */
public class RawPeriod implements Comparable<RawPeriod> {

    private int day;
    private int hour;
    private RealmList<RawLesson> lessons;

    public RawPeriod() {

    }

    public RawPeriod(int day, int hour, RealmList<RawLesson> rawLessons) {
        this.day = day;
        this.hour = hour;
        this.lessons = rawLessons;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public List<RawLesson> getLessons() {
        return lessons;
    }

    public void setLessons(RealmList<RawLesson> lessons) {
        this.lessons = lessons;
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

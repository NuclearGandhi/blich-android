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

package com.blackcracks.blich.data.schedule;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.models.IExpandableGroup;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Period extends RealmObject implements Comparable<Period>, IExpandableGroup<Lesson> {

    private int day;
    private int periodNum;
    private RealmList<Lesson> lessons;

    private Lesson firstLesson;
    @Required private RealmList<Integer> changeTypeColors;

    public Period() {
    }

    public Period(int day, RealmList<Lesson> lessons, int periodNum) {
        this.day = day;
        this.lessons = lessons;
        this.periodNum = periodNum;
        changeTypeColors = new RealmList<>();
    }

    @Override
    public List<Lesson> getItems() {
        return lessons;
    }

    @Override
    public int getItemCount() {
        return lessons != null ? lessons.size() : 0;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getPeriodNum() {
        return periodNum;
    }

    public Lesson getFirstLesson() {
        return firstLesson;
    }

    public void setFirstLesson(Lesson firstLesson) {
        this.firstLesson = firstLesson;
    }

    public List<Integer> getChangeTypeColors() {
        return changeTypeColors;
    }

    public void addChangeTypeColor(@ColorInt int color) {
        if (!changeTypeColors.contains(color))
            changeTypeColors.add(color);
    }

    @Override
    public int compareTo(@NonNull Period o) {
        if (day < o.getDay()) return -1;
        if (day > o.getDay()) return 1;
        if (periodNum < o.getPeriodNum()) return -1;
        if (periodNum == o.getPeriodNum()) return 0;

        return 1;
    }

    public void removeAllNormalLessons() {
        for(int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getModifier() == null) {
                lessons.remove(i);
                i--;
            }
        }
    }
}

package com.blackcracks.blich.data.schedule;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.models.IExpandableGroup;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Period extends RealmObject implements Comparable<Period>, IExpandableGroup<Lesson> {

    private int day;
    private int periodNum;
    private RealmList<Lesson> lessons;

    private Lesson firstLesson;
    private RealmList<Integer> changeTypeColors;

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

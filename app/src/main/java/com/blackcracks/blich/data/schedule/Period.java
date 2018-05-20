package com.blackcracks.blich.data.schedule;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.models.IExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class Period implements Comparable<Period>, IExpandableGroup<Lesson> {

    private List<Lesson> lessons;

    private int periodNum;
    private Lesson firstLesson;
    private List<Integer> changeTypeColors;

    public Period(List<Lesson> lessons, int periodNum) {
        this.lessons = lessons;
        this.periodNum = periodNum;
        changeTypeColors = new ArrayList<>();
    }

    @Override
    public List<Lesson> getItems() {
        return lessons;
    }

    @Override
    public int getItemCount() {
        return lessons != null ? lessons.size() : 0;
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
        if (periodNum < o.getPeriodNum()) return -1;
        else if (periodNum == o.getPeriodNum()) return 0;
        else return 1;
    }
}

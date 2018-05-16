package com.blackcracks.blich.data.schedule;

import android.os.Parcel;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class Period extends ExpandableGroup<Lesson> implements Comparable<Period> {

    private int periodNum;
    private Lesson firstLesson;
    private List<Integer> changeTypeColors;

    public Period(String title, List<Lesson> items, int periodNum) {
        super(title, items);
        this.periodNum = periodNum;
        changeTypeColors = new ArrayList<>();
    }

    public Period(Parcel in) {
        super(in);
    }

    public int getPeriodNum() {
        return periodNum;
    }

    public void setPeriodNum(int periodNum) {
        this.periodNum = periodNum;
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

    public void setChangeTypeColors(List<Integer> changeTypeColors) {
        this.changeTypeColors = changeTypeColors;
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

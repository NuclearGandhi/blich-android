package com.blackcracks.blich.data.schedule;

import android.os.Parcel;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Period extends ExpandableGroup<Lesson> {

    private int day;
    private int period;
    private String changeTypes;

    public Period(String title, List<Lesson> items, int day, int period, String changeTypes) {
        super(title, items);
        this.day = day;
        this.period = period;
        this.changeTypes = changeTypes;
    }

    public Period(Parcel in) {
        super(in);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getChangeTypes() {
        return changeTypes;
    }

    public void setChangeTypes(String changeTypes) {
        this.changeTypes = changeTypes;
    }
}

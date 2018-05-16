/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data;

import com.blackcracks.blich.data.raw.Change;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.raw.Exam;
import com.blackcracks.blich.data.raw.RawPeriod;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * The overall schedule data object, used to easily insert data into realm.
 */
public class BlichData extends RealmObject {

    private int classId;
    @Required private RealmList<RawPeriod> periods;
    @Required private RealmList<Change> changes;
    @Required private RealmList<Event> events;
    @Required private RealmList<Exam> exams;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public RealmList<RawPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(RealmList<RawPeriod> RawPeriods) {
        this.periods = RawPeriods;
    }

    public RealmList<Change> getChanges() {
        return changes;
    }

    public void setChanges(RealmList<Change> changes) {
        this.changes = changes;
    }

    public RealmList<Event> getEvents() {
        return events;
    }

    public void setEvents(RealmList<Event> events) {
        this.events = events;
    }

    public RealmList<Exam> getExams() {
        return exams;
    }

    public void setExams(RealmList<Exam> exams) {
        this.exams = exams;
    }
}

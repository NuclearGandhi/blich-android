/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.List;

public class ScheduleResult {

    private List<Hour> mHours;
    private List<Change> mChanges;
    private List<Event> mEvents;
    private List<Exam> mExams;

    public ScheduleResult(List<Hour> hours, List<Change> changes, List<Event> events, List<Exam> exams) {
        mHours = hours;
        mChanges = changes;
        mEvents = events;
        mExams = exams;
    }

    public List<Hour> getHours() {
        return mHours;
    }

    public List<Change> getChanges() {
        return mChanges;
    }

    public List<Event> getEvents() {
        return mEvents;
    }

    public List<Exam> getExams() {
        return mExams;
    }

    public boolean isDataValid() {
        return mHours != null && mChanges != null && mEvents != null && mExams != null;
    }
}

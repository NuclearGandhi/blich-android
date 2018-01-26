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

    public ScheduleResult(List<Hour> hours, List<Change> changes, List<Event> events) {
        mHours = hours;
        mChanges = changes;
        mEvents = events;
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
}

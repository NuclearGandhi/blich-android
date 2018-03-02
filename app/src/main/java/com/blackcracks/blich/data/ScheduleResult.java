/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data;

import java.util.List;

/**
 * A class to hold schedule result from realm.
 */
public class ScheduleResult {

    private List<Hour> mHours;
    private List<DatedLesson> mDatedLessons;

    public ScheduleResult(List<Hour> hours, List<DatedLesson> datedLessons) {
        mHours = hours;
        mDatedLessons = datedLessons;
    }

    public List<Hour> getHours() {
        return mHours;
    }

    public List<DatedLesson> getDatedLessons() {
        return mDatedLessons;
    }

    /**
     * Check if the data in {@link ScheduleResult} is valid.
     *
     * @return {@code true} the data is valid.
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean isDataValid() {
        if (mHours == null || mDatedLessons == null) return false;
        if (mHours.size() == 0 && mDatedLessons.size() == 0) return false;
        return true;
    }
}

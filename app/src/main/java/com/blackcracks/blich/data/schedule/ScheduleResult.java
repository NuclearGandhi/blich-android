/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.schedule;

import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;

import java.util.List;

/**
 * A class to hold schedule result from realm.
 */
public class ScheduleResult {

    private List<RawPeriod> mRawPeriods;
    private List<DatedLesson> mDatedLessons;

    public ScheduleResult(List<RawPeriod> RawPeriods, List<DatedLesson> datedLessons) {
        mRawPeriods = RawPeriods;
        mDatedLessons = datedLessons;
    }

    public List<RawPeriod> getRawPeriods() {
        return mRawPeriods;
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
        if (mRawPeriods == null || mDatedLessons == null) return false;
        if (mRawPeriods.size() == 0 && mDatedLessons.size() == 0) return false;

        for (RawPeriod RawPeriod : mRawPeriods) {
            for (RawLesson rawLesson : RawPeriod.getRawLessons()) {
                if (!rawLesson.isValid()) return false;
            }
        }

        for(DatedLesson datedLesson : mDatedLessons) {
            if (!datedLesson.isValid()) return false;
        }
        return true;
    }
}

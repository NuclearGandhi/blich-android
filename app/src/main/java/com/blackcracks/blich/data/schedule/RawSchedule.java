/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.schedule;

import com.blackcracks.blich.data.raw.ModifiedLesson;
import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;

import java.util.List;

/**
 * A class to hold schedule result from realm.
 */
public class RawSchedule {

    private List<RawPeriod> mRawPeriods;
    private List<ModifiedLesson> mModifiedLessons;

    public RawSchedule(List<RawPeriod> RawPeriods, List<ModifiedLesson> modifiedLessons) {
        mRawPeriods = RawPeriods;
        mModifiedLessons = modifiedLessons;
    }

    public List<RawPeriod> getRawPeriods() {
        return mRawPeriods;
    }

    public List<ModifiedLesson> getModifiedLessons() {
        return mModifiedLessons;
    }

    /**
     * Check if the data in {@link RawSchedule} is valid.
     *
     * @return {@code true} the data is valid.
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean isDataValid() {
        if (mRawPeriods == null || mModifiedLessons == null) return false;
        if (mRawPeriods.size() == 0 && mModifiedLessons.size() == 0) return false;

        for (RawPeriod RawPeriod : mRawPeriods) {
            for (RawLesson rawLesson : RawPeriod.getLessons()) {
                if (!rawLesson.isValid()) return false;
            }
        }

        for(ModifiedLesson modifiedLesson : mModifiedLessons) {
            if (!modifiedLesson.isValid()) return false;
        }
        return true;
    }
}

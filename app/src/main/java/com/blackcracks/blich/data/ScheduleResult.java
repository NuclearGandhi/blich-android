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

    public boolean isDataValid() {
        if (mHours == null || mDatedLessons == null) return false;
        if (mHours.size() == 0 && mDatedLessons.size() == 0) return false;
        return true;
    }
}

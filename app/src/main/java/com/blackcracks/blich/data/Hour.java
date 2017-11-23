/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import java.util.List;

/**
 * Created by Administrator on 11/23/2017.
 */

public class Hour {

    private int mHour;
    private List<Lesson> mLessons;

    public Hour(int hour, List<Lesson> lessons) {
        mHour = hour;
        mLessons = lessons;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        mHour = hour;
    }

    public List<Lesson> getLessons() {
        return mLessons;
    }

    public void setLessons(List<Lesson> lessons) {
        mLessons = lessons;
    }
}

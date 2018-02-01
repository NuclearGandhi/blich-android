/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.util;

import android.support.annotation.Nullable;

import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Hour;
import com.blackcracks.blich.data.Lesson;
import com.blackcracks.blich.data.ScheduleResult;

import java.util.List;

import timber.log.Timber;

public class RealmScheduleHelper {
    private List<Hour> mHours;
    private List<DatedLesson> mDatedLessons;

    private List<Event> mEvents;
    private boolean mIsDataValid;

    public RealmScheduleHelper(ScheduleResult data) {
        switchData(data);
    }

    public void switchData(ScheduleResult data) {
        //Check if the data is valid
        try {
            mIsDataValid = data != null && data.isDataValid();
        } catch (IllegalStateException e) { //In case Realm instance has been closed
            mIsDataValid = false;
            Timber.d("Realm has been closed");
        }

        if (mIsDataValid) {
            mHours = data.getHours();
            mDatedLessons = data.getDatedLessons();
        }
    }

    public boolean isDataValid() {
        return mIsDataValid;
    }

    public Hour getHour(int position) {
        return mHours.get(position);
    }

    public @Nullable
    Lesson getLesson(int position, int childPos) {
        if (!mIsDataValid) return null;
        List<Lesson> lessons = getHour(position).getLessons();
        if (lessons != null && lessons.size() > childPos) return lessons.get(childPos);
        return null;
    }

    public boolean isHourSingleEvent(Hour hour) {
        for (DatedLesson lesson :
                mDatedLessons) {
            if (lesson instanceof Event &&
                    lesson.isEqualToHour(hour.getHour()) &&
                    !lesson.isReplacing()) {
                return true;
            }
        }
        return false;
    }

    private int getNonReplacingLessonsCount(Hour hour) {
        int count = 0;
        for (DatedLesson datedLesson :
                mDatedLessons) {
            if (!datedLesson.isReplacing() &&
                    datedLesson.isEqualToHour(hour.getHour())) count++;
        }
        return count;
    }

    public int getHourCount() {
        if (mIsDataValid) {
            return mHours.size();
        } else {
            return 0;
        }
    }

    public int getChildCount(int position) {
        if (mIsDataValid) {
            Hour hour = getHour(position);
            if (isHourSingleEvent(hour)) {
                return 0;
            }
            return hour.getLessons().size() + getNonReplacingLessonsCount(hour);
        } else {
            return 0;
        }
    }

    public int getLessonCount(int position) {
        if (mIsDataValid) {
            return getHour(position).getLessons().size();
        } else {
            return 0;
        }
    }
}

/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter.helper;

import android.support.annotation.Nullable;

import com.blackcracks.blich.data.schedule.DatedLesson;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.raw.Hour;
import com.blackcracks.blich.data.raw.Lesson;
import com.blackcracks.blich.data.schedule.ScheduleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * A helper class to easily extract information from given data.
 */
public class RealmScheduleHelper {
    private List<Hour> mHours;
    private List<DatedLesson> mDatedLessons;

    private boolean mIsDataValid;

    @SuppressWarnings("SameParameterValue")
    public RealmScheduleHelper(ScheduleResult data) {
        switchData(data);
    }

    /**
     * Switch to the given data
     *
     * @param data data to switch to.
     */
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
            buildEmptyHours();
        }
    }

    /**
     * Add empty hours to the hour list, so non replacing dated lessons can
     * be shown.
     */
    private void buildEmptyHours() {
        for (DatedLesson datedLesson :
                mDatedLessons) {
            //if (!datedLesson.isAReplacer()) {
            for (int i = datedLesson.getBeginHour(); i <= datedLesson.getEndHour(); i++) {
                if (getHourByNum(i) == null) {
                    Hour hour = new Hour();
                    hour.setHour(i);
                    mHours.add(hour);
                }
                //}
            }
        }
        Collections.sort(mHours);
    }

    private @Nullable
    Hour getHourByNum(int hourNum) {
        for (Hour hour :
                mHours) {
            if (hour.getHour() == hourNum) return hour;
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    /**
     * Get a {@link DatedLesson} replacement for the lesson, if it exists.
     *
     * @param toReplace a {@link Lesson} to replace.
     * @return replaced {@link DatedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    DatedLesson getLessonReplacement(int hour, Lesson toReplace) {
        for (DatedLesson datedLesson :
                mDatedLessons) {
            if (datedLesson.isEqualToHour(hour) && datedLesson.canReplaceLesson(toReplace)) {
                return datedLesson;
            }
        }

        return null;
    }

    /**
     * Get all the {@link DatedLesson}s in the specified hour.
     *
     * @return a list of {@link DatedLesson}s.
     */
    public List<DatedLesson> getDatedLessons(Hour hour) {
        List<DatedLesson> lessons = new ArrayList<>();
        for (DatedLesson datedLesson :
                mDatedLessons) {
            if (datedLesson.isEqualToHour(hour.getHour())) {
                lessons.add(datedLesson);
            }
        }
        return lessons;
    }

    /**
     * Get a single replacing {@link DatedLesson} in the specified hour.
     *
     * @return non replacing {@link DatedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    DatedLesson getNonReplacingLesson(Hour hour) {
        for (DatedLesson lesson :
                mDatedLessons) {
            if (lesson.isEqualToHour(hour.getHour()) && !lesson.isAReplacer() && lesson instanceof Event) {
                return lesson;
            }
        }
        return null;
    }

    /**
     * Get all the non replacing lessons, and lessons that come in addition to
     * (see {@link #canReplaceInList(DatedLesson, List)}) the specified hour.
     *
     * @return a list of {@link DatedLesson}s.
     */
    public List<DatedLesson> getAdditionalLessons(Hour hour) {
        List<Lesson> lessons = hour.getLessons();
        if (lessons == null)
            return getDatedLessons(hour);

        List<DatedLesson> nonReplacingLessons = new ArrayList<>();
        for (DatedLesson datedLesson :
                getDatedLessons(hour)) {
            if (!datedLesson.isAReplacer() || !canReplaceInList(datedLesson, lessons)) {
                nonReplacingLessons.add(datedLesson);
            }
        }
        return nonReplacingLessons;
    }

    /**
     * Get the count additional lessons in the specified hour.
     *
     * @param hour a period.
     * @return count of additional lessons.
     */
    private int getAdditionalLessonsCount(Hour hour) {
        return getAdditionalLessons(hour).size();
    }

    /**
     * Check if the given {@link DatedLesson} can replace any {@link Lesson} in the
     * given list.
     *
     * @param datedLesson a {@link DatedLesson}.
     * @param lessons     a list of {@link Lesson}s.
     * @return {@code true} the {@code datedLesson} can replace.
     */
    private boolean canReplaceInList(DatedLesson datedLesson, List<Lesson> lessons) {
        for (Lesson lesson :
                lessons) {
            if (datedLesson.canReplaceLesson(lesson)) return true;
        }
        return false;
    }

    /**
     * Get the count of hours.
     *
     * @return hours count.
     */
    public int getHourCount() {
        if (mIsDataValid) {
            return mHours.size();
        } else {
            return 0;
        }
    }

    /**
     * Get the count of children for the hour in the specified position.
     *
     * @param position position of hour.
     * @return count of children.
     */
    public int getChildCount(int position) {
        if (mIsDataValid) {
            Hour hour = getHour(position);
            if (getNonReplacingLesson(hour) != null) {
                return 0;
            }
            return getLessonCount(hour) + getAdditionalLessonsCount(hour);
        } else {
            return 0;
        }
    }

    /**
     * Get the count of normal lessons in the given hour.
     *
     * @param hour an {@link Hour}.
     * @return count of lessons.
     */
    public int getLessonCount(Hour hour) {
        if (!mIsDataValid) return 0;
        List<Lesson> lessons = hour.getLessons();
        if (lessons == null) {
            return 0;
        }
        return lessons.size();
    }

    /**
     * Get the count of normal lesson in the hour in the specified position.
     *
     * @param position position of hour.
     * @return count of lessons.
     */
    public int getLessonCount(int position) {
        return getLessonCount(getHour(position));
    }
}

/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter.helper;

import android.support.annotation.Nullable;

import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.DatedLesson;
import com.blackcracks.blich.data.raw.Event;
import com.blackcracks.blich.data.schedule.ScheduleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * A helper class to easily extract information from given data.
 */
public class RealmScheduleHelper {
    private List<RawPeriod> mRawPeriods;
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
            mRawPeriods = data.getRawPeriods();
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
                    RawPeriod RawPeriod = new RawPeriod();
                    RawPeriod.setHour(i);
                    mRawPeriods.add(RawPeriod);
                }
                //}
            }
        }
        Collections.sort(mRawPeriods);
    }

    private @Nullable
    RawPeriod getHourByNum(int hourNum) {
        for (RawPeriod RawPeriod :
                mRawPeriods) {
            if (RawPeriod.getHour() == hourNum) return RawPeriod;
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDataValid() {
        return mIsDataValid;
    }

    public RawPeriod getHour(int position) {
        return mRawPeriods.get(position);
    }

    public @Nullable
    RawLesson getLesson(int position, int childPos) {
        if (!mIsDataValid) return null;
        List<RawLesson> rawLessons = getHour(position).getRawLessons();
        if (rawLessons != null && rawLessons.size() > childPos) return rawLessons.get(childPos);
        return null;
    }

    /**
     * Get a {@link DatedLesson} replacement for the lesson, if it exists.
     *
     * @param toReplace a {@link RawLesson} to replace.
     * @return replaced {@link DatedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    DatedLesson getLessonReplacement(int hour, RawLesson toReplace) {
        for (DatedLesson datedLesson :
                mDatedLessons) {
            if (datedLesson.isEqualToHour(hour) && datedLesson.canReplaceLesson(toReplace)) {
                return datedLesson;
            }
        }

        return null;
    }

    /**
     * Get all the {@link DatedLesson}s in the specified RawPeriod.
     *
     * @return a list of {@link DatedLesson}s.
     */
    public List<DatedLesson> getDatedLessons(RawPeriod RawPeriod) {
        List<DatedLesson> lessons = new ArrayList<>();
        for (DatedLesson datedLesson :
                mDatedLessons) {
            if (datedLesson.isEqualToHour(RawPeriod.getHour())) {
                lessons.add(datedLesson);
            }
        }
        return lessons;
    }

    /**
     * Get a single replacing {@link DatedLesson} in the specified RawPeriod.
     *
     * @return non replacing {@link DatedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    DatedLesson getNonReplacingLesson(RawPeriod RawPeriod) {
        for (DatedLesson lesson :
                mDatedLessons) {
            if (lesson.isEqualToHour(RawPeriod.getHour()) && !lesson.isAReplacer() && lesson instanceof Event) {
                return lesson;
            }
        }
        return null;
    }

    /**
     * Get all the non replacing lessons, and lessons that come in addition to
     * (see {@link #canReplaceInList(DatedLesson, List)}) the specified RawPeriod.
     *
     * @return a list of {@link DatedLesson}s.
     */
    public List<DatedLesson> getAdditionalLessons(RawPeriod RawPeriod) {
        List<RawLesson> rawLessons = RawPeriod.getRawLessons();
        if (rawLessons == null)
            return getDatedLessons(RawPeriod);

        List<DatedLesson> nonReplacingLessons = new ArrayList<>();
        for (DatedLesson datedLesson :
                getDatedLessons(RawPeriod)) {
            if (!datedLesson.isAReplacer() || !canReplaceInList(datedLesson, rawLessons)) {
                nonReplacingLessons.add(datedLesson);
            }
        }
        return nonReplacingLessons;
    }

    /**
     * Get the count additional lessons in the specified RawPeriod.
     *
     * @param RawPeriod a period.
     * @return count of additional lessons.
     */
    private int getAdditionalLessonsCount(RawPeriod RawPeriod) {
        return getAdditionalLessons(RawPeriod).size();
    }

    /**
     * Check if the given {@link DatedLesson} can replace any {@link RawLesson} in the
     * given list.
     *
     * @param datedLesson a {@link DatedLesson}.
     * @param rawLessons     a list of {@link RawLesson}s.
     * @return {@code true} the {@code datedLesson} can replace.
     */
    private boolean canReplaceInList(DatedLesson datedLesson, List<RawLesson> rawLessons) {
        for (RawLesson rawLesson :
                rawLessons) {
            if (datedLesson.canReplaceLesson(rawLesson)) return true;
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
            return mRawPeriods.size();
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
            RawPeriod RawPeriod = getHour(position);
            if (getNonReplacingLesson(RawPeriod) != null) {
                return 0;
            }
            return getLessonCount(RawPeriod) + getAdditionalLessonsCount(RawPeriod);
        } else {
            return 0;
        }
    }

    /**
     * Get the count of normal lessons in the given RawPeriod.
     *
     * @param RawPeriod an {@link RawPeriod}.
     * @return count of lessons.
     */
    public int getLessonCount(RawPeriod RawPeriod) {
        if (!mIsDataValid) return 0;
        List<RawLesson> rawLessons = RawPeriod.getRawLessons();
        if (rawLessons == null) {
            return 0;
        }
        return rawLessons.size();
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

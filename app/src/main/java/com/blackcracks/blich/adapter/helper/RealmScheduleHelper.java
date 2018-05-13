/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.adapter.helper;

import android.support.annotation.Nullable;

import com.blackcracks.blich.data.raw.RawLesson;
import com.blackcracks.blich.data.raw.RawPeriod;
import com.blackcracks.blich.data.schedule.ModifiedLesson;
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
    private List<ModifiedLesson> mModifiedLessons;

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
            mModifiedLessons = data.getModifiedLessons();
            buildEmptyHours();
        }
    }

    /**
     * Add empty hours to the hour list, so non replacing dated lessons can
     * be shown.
     */
    private void buildEmptyHours() {
        for (ModifiedLesson modifiedLesson :
                mModifiedLessons) {
            //if (!modifiedLesson.isAReplacer()) {
            for (int i = modifiedLesson.getBeginHour(); i <= modifiedLesson.getEndHour(); i++) {
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
     * Get a {@link ModifiedLesson} replacement for the lesson, if it exists.
     *
     * @param toReplace a {@link RawLesson} to replace.
     * @return replaced {@link ModifiedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    ModifiedLesson getLessonReplacement(int hour, RawLesson toReplace) {
        for (ModifiedLesson modifiedLesson :
                mModifiedLessons) {
            if (modifiedLesson.isEqualToHour(hour) && modifiedLesson.canReplaceLesson(toReplace)) {
                return modifiedLesson;
            }
        }

        return null;
    }

    /**
     * Get all the {@link ModifiedLesson}s in the specified RawPeriod.
     *
     * @return a list of {@link ModifiedLesson}s.
     */
    public List<ModifiedLesson> getDatedLessons(RawPeriod RawPeriod) {
        List<ModifiedLesson> lessons = new ArrayList<>();
        for (ModifiedLesson modifiedLesson :
                mModifiedLessons) {
            if (modifiedLesson.isEqualToHour(RawPeriod.getHour())) {
                lessons.add(modifiedLesson);
            }
        }
        return lessons;
    }

    /**
     * Get a single replacing {@link ModifiedLesson} in the specified RawPeriod.
     *
     * @return non replacing {@link ModifiedLesson}.
     * {@code null} if none exist.
     */
    public @Nullable
    ModifiedLesson getNonReplacingLesson(RawPeriod RawPeriod) {
        for (ModifiedLesson lesson :
                mModifiedLessons) {
            if (lesson.isEqualToHour(RawPeriod.getHour()) && !lesson.isAReplacer() && lesson instanceof Event) {
                return lesson;
            }
        }
        return null;
    }

    /**
     * Get all the non replacing lessons, and lessons that come in addition to
     * (see {@link #canReplaceInList(ModifiedLesson, List)}) the specified RawPeriod.
     *
     * @return a list of {@link ModifiedLesson}s.
     */
    public List<ModifiedLesson> getAdditionalLessons(RawPeriod RawPeriod) {
        List<RawLesson> rawLessons = RawPeriod.getRawLessons();
        if (rawLessons == null)
            return getDatedLessons(RawPeriod);

        List<ModifiedLesson> nonReplacingLessons = new ArrayList<>();
        for (ModifiedLesson modifiedLesson :
                getDatedLessons(RawPeriod)) {
            if (!modifiedLesson.isAReplacer() || !canReplaceInList(modifiedLesson, rawLessons)) {
                nonReplacingLessons.add(modifiedLesson);
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
     * Check if the given {@link ModifiedLesson} can replace any {@link RawLesson} in the
     * given list.
     *
     * @param modifiedLesson a {@link ModifiedLesson}.
     * @param rawLessons     a list of {@link RawLesson}s.
     * @return {@code true} the {@code modifiedLesson} can replace.
     */
    private boolean canReplaceInList(ModifiedLesson modifiedLesson, List<RawLesson> rawLessons) {
        for (RawLesson rawLesson :
                rawLessons) {
            if (modifiedLesson.canReplaceLesson(rawLesson)) return true;
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

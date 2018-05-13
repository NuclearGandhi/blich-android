/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.schedule;

import android.support.annotation.ColorInt;

import com.blackcracks.blich.data.raw.RawLesson;

import java.util.Date;

/**
 * An interface for all changes in the schedule and their common behavior.
 */
public interface ModifiedLesson {

    /**
     * Build a label to show to the user.
     *
     * @return a label.
     */
    String buildName();

    /**
     * Get the change type.
     *
     * @return change type.
     */
    String getType();

    /**
     * Get the lesson's date
     *
     * @return a {@link Date}.
     */
    Date getDate();

    boolean isValid();

    /** Compare between the current lesson and a given hour.
     * @param hour hour - period to compare to.
     * @return {@code true} the lesson is taking place in the given hour.
     */
    boolean isEqualToHour(int hour);

    /**
     * @return {@code true} the lesson replaces another lesson.
     *         {@code false} the lesson comes in addition to other lessons.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isAReplacer();

    /**
     * Can the current lesson replace the given {@link RawLesson}.
     *
     * @param toReplace {@link RawLesson} to replace
     * @return {@code true} replaces the given {@link RawLesson}.
     */
    boolean canReplaceLesson(RawLesson toReplace);

    /**
     * Get the period the lesson begins.
     *
     * @return the beginning period.
     */
    int getBeginHour();

    /**
     * Get the period the lesson ends.
     *
     * @return the ending period.
     */
    int getEndHour();

    /**
     * Get a color representing the lesson.
     *
     * @return a {@link ColorInt}.
     */
    @ColorInt int getColor();
}

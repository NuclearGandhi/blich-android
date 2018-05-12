/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.data.exam;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An interface to be used in {@link com.blackcracks.blich.adapter.ExamAdapter} to easily handle
 * {@link MonthDivider} and {@link GenericExam}.
 */
public interface ExamItem {

    @Retention(SOURCE)
    @IntDef({TYPE_EXAM, TYPE_MONTH})
    @interface Type{}

    int TYPE_EXAM = 0;
    int TYPE_MONTH = 1;

    /**
     * Get the {@link Type} of the item.
     *
     * @return a {@link Type}.
     */
    @Type int getType();
}

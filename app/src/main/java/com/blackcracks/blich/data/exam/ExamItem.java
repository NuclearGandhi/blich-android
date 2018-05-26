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
 * {@link MonthDivider} and {@link Exam}.
 */
public interface ExamItem {

    @Retention(SOURCE)
    @IntDef({TYPE_EXAM, TYPE_MONTH})
    @interface ExamItemType {}

    int TYPE_EXAM = 0;
    int TYPE_MONTH = 1;

    /**
     * Get the {@link ExamItemType} of the item.
     *
     * @return a {@link ExamItemType}.
     */
    @ExamItemType
    int getType();
}

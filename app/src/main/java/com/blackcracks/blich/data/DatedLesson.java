/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.annotation.ColorInt;

import java.util.Date;

public interface DatedLesson {

    String buildName();
    String getType();

    Date getDate();

    boolean isEqualToHour(int hour);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isAReplacer();

    boolean canReplaceLesson(Lesson toReplace);

    int getBeginHour();
    int getEndHour();

    @ColorInt int getColor(Context context);
}

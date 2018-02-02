/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import android.content.Context;
import android.support.annotation.ColorInt;

public interface DatedLesson {

    String buildName();
    String getType();

    boolean isEqualToHour(int hour);
    boolean isAReplacer();
    boolean canReplaceLesson(Lesson toReplace);

    @ColorInt int getColor(Context context);
}

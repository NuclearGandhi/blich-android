/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

public interface ExamItem {

    public static final int TYPE_EXAM = 0;
    public static final int TYPE_MONTH = 1;

    int getType();
}

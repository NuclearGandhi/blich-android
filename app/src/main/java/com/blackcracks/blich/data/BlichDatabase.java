/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.data;

import com.couchbase.lite.Database;

public class BlichDatabase {

    public static Database sDatabase;

    public static final String DATABASE_NAME = "blich";

    public static final String SCHEDULE_DOC_ID = "schedule";
    public static final String SCHEDULE_VIEW_ID = "schedule";
    public static final String TEACHER_VIEW_ID = "teacher";

    public static final String SCHEDULE_KEY = "schedule";
    public static final String HOURS_KEY = "hours";
    public static final String LESSONS_KEY = "lessons";

    public static final String HOUR_KEY = "hour";
    public static final String DAY_KEY = "day";

    public static final String SUBJECT_KEY = "subject";
    public static final String CLASSROOM_KEY = "classroom";
    public static final String TEACHER_KEY = "teacher";
    public static final String LESSON_TYPE_KEY = "lesson_type";

    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_CANCELED = "canceled";
    public static final String TYPE_EXAM = "exam";
    public static final String TYPE_EVENT = "event";
    public static final String TYPE_CHANGE = "change";

}

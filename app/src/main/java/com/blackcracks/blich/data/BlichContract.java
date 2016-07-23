package com.blackcracks.blich.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BlichContract {

    public static final String CONTENT_AUTHORITY = "com.blackcracks.blich";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SCHEDULE = "schedule";
    public static final String PATH_CLASS = "class";

    public static final class ScheduleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SCHEDULE)
                .build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCHEDULE;
        @SuppressWarnings("unused")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCHEDULE;

        public static final String TABLE_NAME = "schedule";

        public static final String COL_CLASS_SETTINGS = "class_settings";
        public static final String COL_DAY = "day";
        public static final String COL_HOUR = "hour";

        public static final String COL_SUBJECT = "subject";
        public static final String COL_CLASSROOM = "classroom";
        public static final String COL_TEACHER = "teacher";
        public static final String COL_LESSON_TYPE = "lesson_type";

        public static final String LESSON_TYPE_NORMAL = "normal";
        public static final String LESSON_TYPE_CANCELED = "canceled";
        public static final String LESSON_TYPE_CHANGED = "changed";
        public static final String LESSON_TYPE_EXAM = "exam";
        public static final String LESSON_TYPE_EVENT = "event";

        public static int getDayFromUri(Uri uri) {
            return Integer.parseInt(uri.getLastPathSegment());
        }

        public static Uri buildScheduleWithDayUri(int day) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(day))
                    .build();
        }
    }

    public static final class ClassEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CLASS)
                .build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLASS;
        @SuppressWarnings("unused")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CLASS;

        public static final String TABLE_NAME = "classes";

        public static final String COL_CLASS_INDEX = "class_index";
        public static final String COL_GRADE = "grade";
        public static final String COL_GRADE_INDEX = "grade_index";
    }
}

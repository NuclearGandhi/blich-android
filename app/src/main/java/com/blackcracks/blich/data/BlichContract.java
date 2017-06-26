package com.blackcracks.blich.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BlichContract {

    public static final String CONTENT_AUTHORITY = "com.blackcracks.blich";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_SCHEDULE = "schedule";
    static final String PATH_LESSON = "lesson";
    static final String PATH_EXAMS = "exams";
    static final String PATH_NEWS = "news";
    static final String PATH_CLASS = "class";

    /**
     * Table that contains the schedule:
     * -day
     * -hour
     *
     * -lesson_count
     * -events
     * -general_subject
     */
    public static final class ScheduleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SCHEDULE)
                .build();

        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCHEDULE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCHEDULE;

        public static final String TABLE_NAME = "schedule";

        public static final String COL_DAY = "day";
        public static final String COL_HOUR = "hour";

        public static final String COL_LESSON_COUNT = "lesson_count";
        public static final String COL_EVENTS = "events";

        public static int getLessonNumFromUri(Uri uri) {
            return Integer.parseInt(uri.getLastPathSegment());
        }

        public static Uri buildScheduleWithLessonUri(int lessonNum) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(lessonNum))
                    .build();
        }
    }

    /**
     * Table the contains the lessons:
     * -day
     * -hour
     * -lesson_num
     *
     * -subject
     * -classroom
     * -teacher
     * -lesson_type
     */
    public static final class LessonEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LESSON)
                .build();

        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LESSON;
        @SuppressWarnings("unused")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LESSON;

        public static final String TABLE_NAME = "lesson";

        public static final String COL_DAY = "day";
        public static final String COL_HOUR = "hour";
        public static final String COL_LESSON_NUM = "lesson_num";

        public static final String COL_SUBJECT = "subject";
        public static final String COL_CLASSROOM = "classroom";
        public static final String COL_TEACHER = "teacher";
        public static final String COL_LESSON_TYPE = "lesson_type";

        public static final String LESSON_TYPE_NORMAL = "normal";
        public static final String LESSON_TYPE_CANCELED = "canceled";
        public static final String LESSON_TYPE_CHANGED = "changed";
        public static final String LESSON_TYPE_EXAM = "exam";
        public static final String LESSON_TYPE_EVENT = "event";
    }

    /**
     * Table that contains the exams:
     * -date
     * -subject
     * -teacher
     */
    public static final class ExamsEntry implements  BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_EXAMS)
                .build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXAMS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXAMS;

        public static final String TABLE_NAME = "exams";

        public static final String COL_DATE = "date";
        public static final String COL_SUBJECT = "subject";
        public static final String COL_TEACHER = "teacher";
    }

    /**
     * Table that contains the news:
     * -category
     *
     * -title
     * -body
     * -author
     * -date
     */
    public static final class NewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_NEWS)
                .build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        @SuppressWarnings("unused")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static final String TABLE_NAME = "news";

        public static final String COL_CATEGORY = "category";
        public static final String COL_TITLE = "title";
        public static final String COL_BODY = "text";
        public static final String COL_AUTHOR = "author";
        public static final String COL_DATE = "date";
    }

    /**
     * Table that contains the classes:
     * -class_id
     *
     * -grade
     * -grade_index
     */
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

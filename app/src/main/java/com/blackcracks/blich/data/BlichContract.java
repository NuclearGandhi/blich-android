package com.blackcracks.blich.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BlichContract {

    public static final String CONTENT_AUTHORITY = "com.blackcracks.blich";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


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
     * This class does not implement {@link BaseColumns} because an autoincrement key with a different
     * name than '_id' is needed.
     */


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

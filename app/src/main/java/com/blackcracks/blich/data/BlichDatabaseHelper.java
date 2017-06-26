package com.blackcracks.blich.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.blackcracks.blich.data.BlichContract.*;
public class BlichDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;

    public static final String DATABASE_NAME = "blich.db";

    private final String SQL_CREATE_SCHEDULE_TABLE = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " (" +
            ScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleEntry.COL_DAY + " INTEGER NOT NULL, " +
            ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +

            ScheduleEntry.COL_LESSON_COUNT + " INTEGER, " +
            ScheduleEntry.COL_EVENTS + " TEXT NOT NULL, " +
            "UNIQUE (" + ScheduleEntry.COL_DAY + ", " + ScheduleEntry.COL_HOUR +
            ") ON CONFLICT REPLACE);";

    private final String SQL_CREATE_LESSON_TABLE = "CREATE TABLE " + LessonEntry.TABLE_NAME + " (" +
            LessonEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            LessonEntry.COL_DAY + " INTEGER NOT NULL, " +
            LessonEntry.COL_HOUR + " INTEGER NOT NULL, " +
            LessonEntry.COL_LESSON_NUM + " INTEGER NOT NULL, " +

            LessonEntry.COL_SUBJECT + " TEXT NOT NULL, " +
            LessonEntry.COL_CLASSROOM + " TEXT, " +
            LessonEntry.COL_TEACHER + " TEXT, " +
            LessonEntry.COL_LESSON_TYPE + " TEXT NOT NULL, " +
            "UNIQUE (" + LessonEntry.COL_DAY + ", " + LessonEntry.COL_HOUR +
            ", " + LessonEntry.COL_LESSON_NUM + ") ON CONFLICT REPLACE);";

    final String SQL_DROP_SCHEDULE_TABLE = "DROP TABLE " + ScheduleEntry.TABLE_NAME + ";";

    private final String SQL_CREATE_EXAMS_TABLE = "CREATE TABLE " + ExamsEntry.TABLE_NAME + " (" +
            ExamsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ExamsEntry.COL_SUBJECT + " TEXT NOT NULL," +
            ExamsEntry.COL_DATE + " TEXT NOT NULL, " +
            ExamsEntry.COL_TEACHER + " TEXT NOT NULL, " +
            "UNIQUE (" + ExamsEntry.COL_SUBJECT + ", " + ExamsEntry.COL_DATE + ", " +
            ExamsEntry.COL_TEACHER + ") ON CONFLICT REPLACE);";

    private final String SQL_CREATE_NEWS_TABLE = "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
            NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NewsEntry.COL_CATEGORY + " INTEGER NOT NULL, " +
            NewsEntry.COL_TITLE + " TEXT NOT NULL, " +
            NewsEntry.COL_BODY + " TEXT NOT NULL, " +
            NewsEntry.COL_AUTHOR + " TEXT NOT NULL, " +
            NewsEntry.COL_DATE + " TEXT NOT NULL, " +
            "UNIQUE (" + NewsEntry.COL_TITLE + ", " + ExamsEntry.COL_DATE + ", " +
            NewsEntry.COL_AUTHOR + ") ON CONFLICT REPLACE);";

    private final String SQL_CREATE_CLASS_TABLE = "CREATE TABLE " + ClassEntry.TABLE_NAME + " (" +
            ClassEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ClassEntry.COL_CLASS_INDEX + " INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
            ClassEntry.COL_GRADE + " TEXT NOT NULL, " +
            ClassEntry.COL_GRADE_INDEX + " INTEGER);";


    public BlichDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_LESSON_TABLE);
        db.execSQL(SQL_CREATE_EXAMS_TABLE);
        db.execSQL(SQL_CREATE_NEWS_TABLE);
        db.execSQL(SQL_CREATE_CLASS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL(SQL_CREATE_EXAMS_TABLE);
        }
        if (oldVersion < 8) {
            db.execSQL(SQL_CREATE_NEWS_TABLE);
        }
        if (oldVersion < 9) {
            db.execSQL(SQL_DROP_SCHEDULE_TABLE);
            db.execSQL(SQL_CREATE_SCHEDULE_TABLE);

            db.execSQL(SQL_CREATE_LESSON_TABLE);
        }
    }
}

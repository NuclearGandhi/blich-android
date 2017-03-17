package com.blackcracks.blich.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.blackcracks.blich.data.BlichContract.*;
public class BlichDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    public static final String DATABASE_NAME = "blich.db";

    public BlichDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SCHEDULE_TABLE = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " (" +
                ScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ScheduleEntry.COL_CLASS_SETTINGS + " INTEGER NOT NULL, " +
                ScheduleEntry.COL_DAY + " INTEGER NOT NULL, " +
                ScheduleEntry.COL_HOUR + " INTEGER NOT NULL, " +
                ScheduleEntry.COL_SUBJECT + " TEXT NOT NULL, " +
                ScheduleEntry.COL_CLASSROOM + " TEXT, " +
                ScheduleEntry.COL_TEACHER + " TEXT, " +
                ScheduleEntry.COL_LESSON_TYPE + " TEXT NOT NULL, " +
                "UNIQUE (" + ScheduleEntry.COL_DAY + ", " + ScheduleEntry.COL_HOUR +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_EXAMS_TABLE = "CREATE TABLE " + ExamsEntry.TABLE_NAME + " (" +
                ExamsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ExamsEntry.COL_SUBJECT + " TEXT NOT NULL," +
                ExamsEntry.COL_DATE + " TEXT NOT NULL, " +
                ExamsEntry.COL_TEACHER + " TEXT NOT NULL, " +
                "UNIQUE (" + ExamsEntry.COL_SUBJECT + ", " + ExamsEntry.COL_DATE + ", " +
                ExamsEntry.COL_TEACHER + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_CLASS_TABLE = "CREATE TABLE " + ClassEntry.TABLE_NAME + " (" +
                ClassEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ClassEntry.COL_CLASS_INDEX + " INTEGER NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                ClassEntry.COL_GRADE + " TEXT NOT NULL, " +
                ClassEntry.COL_GRADE_INDEX + " INTEGER);";

        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_EXAMS_TABLE);
        db.execSQL(SQL_CREATE_CLASS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {

            final String SQL_CREATE_EXAMS_TABLE = "CREATE TABLE " + ExamsEntry.TABLE_NAME + " (" +
                    ExamsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ExamsEntry.COL_SUBJECT + " TEXT NOT NULL," +
                    ExamsEntry.COL_DATE + " TEXT NOT NULL, " +
                    ExamsEntry.COL_TEACHER + " TEXT NOT NULL, " +
                    "UNIQUE (" + ExamsEntry.COL_SUBJECT + ", " + ExamsEntry.COL_DATE + ", " +
                    ExamsEntry.COL_TEACHER + ") ON CONFLICT REPLACE);";

            db.execSQL(SQL_CREATE_EXAMS_TABLE);
        }
    }
}

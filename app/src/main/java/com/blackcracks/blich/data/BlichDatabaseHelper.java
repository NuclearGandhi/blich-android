package com.blackcracks.blich.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@Deprecated
public class BlichDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 12;

    private static final String DATABASE_NAME = "blich.db";

    private static final String SQL_DROP_SCHEDULE_TABLE = "DROP TABLE schedule;";
    private static final String SQL_DROP_LESSON_TABLE = "DROP TABLE lesson;";
    private static final String SQL_DROP_EXAMS_TABLE = "DROP TABLE exams;";
    private static final String SQL_DROP_CLASSES_TABLE = "DROP TABLE classes;";
    private static final String SQL_DROP_NEWS_TABLE = "DROP TABLE news";

    public BlichDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 12) {
            db.execSQL(SQL_DROP_SCHEDULE_TABLE);
            db.execSQL(SQL_DROP_LESSON_TABLE);
            db.execSQL(SQL_DROP_EXAMS_TABLE);
            db.execSQL(SQL_DROP_CLASSES_TABLE);
            db.execSQL(SQL_DROP_NEWS_TABLE);
        }
    }
}

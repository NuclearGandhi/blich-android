package com.blackcracks.blich;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.ApplicationTestCase;

import com.blackcracks.blich.data.BlichContract;
import com.blackcracks.blich.data.BlichDatabaseHelper;
import com.blackcracks.blich.data.BlichContract.ScheduleEntry;

public class TestDatabase extends ApplicationTestCase<Application> {

    public TestDatabase() {
        super(Application.class);
    }

    public void testScheduleDatabase() {
        BlichDatabaseHelper helper = new BlichDatabaseHelper(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ScheduleEntry.COL_CLASS_SETTINGS, "3");
        values.put(ScheduleEntry.COL_DAY, "1");
        values.put(ScheduleEntry.COL_HOUR, "1");
        values.put(ScheduleEntry.COL_SUBJECT, "English");
        values.put(ScheduleEntry.COL_CLASSROOM, "108");
        values.put(ScheduleEntry.COL_TEACHER, "George");
        values.put(ScheduleEntry.COL_LESSON_TYPE, ScheduleEntry.LESSON_TYPE_NORMAL);

        db.insert(BlichContract.ScheduleEntry.TABLE_NAME, null, values);

        Cursor cursor = db.query(BlichContract.ScheduleEntry.TABLE_NAME,
                new String[]{BlichContract.ScheduleEntry.COL_SUBJECT},
                null, null, null, null, null);
        assertTrue("Cursor is empty", cursor.moveToFirst());
        String subject = cursor.getString(cursor.getColumnIndex(BlichContract.ScheduleEntry.COL_SUBJECT));
        assertEquals("Cursor doesn't contain the right data", "English", subject);

        cursor.close();
        helper.close();
    }
}

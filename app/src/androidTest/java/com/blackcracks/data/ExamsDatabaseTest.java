package com.blackcracks.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.blackcracks.blich.data.BlichContract.ExamsEntry;
import com.blackcracks.blich.data.BlichDatabaseHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ExamsDatabaseTest {

    public static final String LOG_TAG = ExamsDatabaseTest.class.getSimpleName();

    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getContext();
        mContext.deleteDatabase(BlichDatabaseHelper.DATABASE_NAME);
    }

    @Test
    public void dbTestCreateDb() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExamsEntry.COL_DATE, "27.1.2016");
        contentValues.put(ExamsEntry.COL_SUBJECT, "אנגלית");
        contentValues.put(ExamsEntry.COL_START_HOUR, 5);
        contentValues.put(ExamsEntry.COL_END_HOUR, 6);
        contentValues.put(ExamsEntry.COL_TEACHER, "אורלי");
        contentValues.put(ExamsEntry.COL_ROOM, "י1 - 105");

        mContext.getContentResolver().insert(ExamsEntry.CONTENT_URI, contentValues);

        Cursor cursor = mContext.getContentResolver().query(ExamsEntry.CONTENT_URI,
                new String[]{ExamsEntry.COL_SUBJECT},
                null, null, null, null);

        assert cursor != null;
        assertThat(cursor.moveToFirst(), is(true));
        assertThat(cursor.getString(0), is("אנגלית"));

        cursor.close();
    }
}

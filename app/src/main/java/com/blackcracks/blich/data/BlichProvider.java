package com.blackcracks.blich.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blackcracks.blich.data.BlichContract.*;

@SuppressWarnings("ConstantConditions")
public class BlichProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BlichDatabaseHelper mDatabaseHelper;

    private static final int SCHEDULE = 100;
    private static final int SCHEDULE_WITH_DAY = 101;
    private static final int CLASS = 102;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new BlichDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                cursor = db.query(
                        ScheduleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            case SCHEDULE_WITH_DAY: {
                int day = ScheduleEntry.getDayFromUri(uri);
                String daySelection = ScheduleEntry.COL_DAY + " = ?";
                String[] daySelectionArgs = new String[]{Integer.toString(day)};
                cursor = db.query(
                        ScheduleEntry.TABLE_NAME,
                        projection,
                        daySelection,
                        daySelectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            case CLASS: {
                cursor = db.query(
                        ClassEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                return ScheduleEntry.CONTENT_TYPE;
            }
            case SCHEDULE_WITH_DAY: {
                return ScheduleEntry.CONTENT_TYPE;
            }
            case CLASS: {
                return ClassEntry.CONTENT_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                long _id = db.insert(
                        ScheduleEntry.TABLE_NAME,
                        null,
                        values);
                validateId(_id, uri);
                break;
            }
            case CLASS: {
                long _id = db.insert(
                        ClassEntry.TABLE_NAME,
                        null,
                        values);
                validateId(_id, uri);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int returnCount = 0;

        db.delete(ScheduleEntry.TABLE_NAME, null, null);

        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                db.beginTransaction();
                for (ContentValues value : values) {
                    long _id = db.insert(
                            ScheduleEntry.TABLE_NAME,
                            null,
                            value);
                    if (_id != -1) {
                        returnCount++;
                    }
                }
                break;
            }
            case CLASS: {
                db.beginTransaction();
                for (ContentValues value : values) {
                    long _id = db.insert(
                            ClassEntry.TABLE_NAME,
                            null,
                            value);
                    if (_id != -1) {
                        returnCount++;
                    }
                }
                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null, false);
        return returnCount;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                rowsDeleted = db.delete(
                        ScheduleEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            case CLASS: {
                rowsDeleted = db.delete(
                        ClassEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case SCHEDULE: {
                rowsUpdated = db.update(
                        ScheduleEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case CLASS: {
                rowsUpdated = db.update(
                        ClassEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BlichContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, BlichContract.PATH_SCHEDULE, SCHEDULE);
        uriMatcher.addURI(authority, BlichContract.PATH_SCHEDULE + "/#", SCHEDULE_WITH_DAY);
        uriMatcher.addURI(authority, BlichContract.PATH_CLASS, CLASS);

        return uriMatcher;
    }

    private void validateId(long _id, Uri uri) {
        if (_id <= 0) {
            throw new SQLiteException("Failed to insert row into " + uri);
        }
    }
}

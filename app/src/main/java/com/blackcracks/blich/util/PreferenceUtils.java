/*
 * MIT License
 *
 * Copyright (c) 2018 Ido Fang Bentov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.blackcracks.blich.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;

import com.blackcracks.blich.R;

/*
 * A Singleton for managing your SharedPreferences.
 *
 * IMPORTANT: The class is not thread safe. It should work fine in most 
 * circumstances since the write and read operations are fast. However
 * if you call edit for bulk updates and do not commit your changes
 * there is a possibility of data loss if a background thread has modified
 * preferences at the same time.
 */
public class PreferenceUtils {
    private static PreferenceUtils sSharedPrefs;
    private Context mContext;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;

    private static final SparseArray<Object> sDefaultValues = new SparseArray<>();

    private PreferenceUtils(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;

        sDefaultValues.put(R.string.pref_is_first_launch_key, false);
        sDefaultValues.put(R.string.pref_is_syncing_key, true);
        sDefaultValues.put(R.string.pref_app_version_key, 1);
        sDefaultValues.put(R.string.pref_user_class_group_key, mContext.getResources().getInteger(R.integer.pref_user_class_group_default));

        sDefaultValues.put(R.string.pref_theme_background_key, mContext.getResources().getBoolean(R.bool.pref_theme_background_default));
        sDefaultValues.put(R.string.pref_theme_lesson_canceled_key, ContextCompat.getColor(mContext, R.color.lesson_canceled));
        sDefaultValues.put(R.string.pref_theme_lesson_changed_key, ContextCompat.getColor(mContext, R.color.lesson_changed));
        sDefaultValues.put(R.string.pref_theme_lesson_exam_key, ContextCompat.getColor(mContext, R.color.lesson_exam));
        sDefaultValues.put(R.string.pref_theme_lesson_event_key, ContextCompat.getColor(mContext, R.color.lesson_event));

        sDefaultValues.put(R.string.pref_notification_toggle_key, mContext.getResources().getBoolean(R.bool.pref_notification_toggle_default));

        sDefaultValues.put(R.string.pref_filter_toggle_key, mContext.getResources().getBoolean(R.bool.pref_filter_toggle_default));
        sDefaultValues.put(R.string.pref_filter_select_key, "");

        sDefaultValues.put(R.string.pref_exam_reminder_hour, 8);
        sDefaultValues.put(R.string.pref_exam_reminder_minutes, 15);
    }

    public static PreferenceUtils getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new PreferenceUtils(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static PreferenceUtils getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
    }

    public Object getDefaultValue(@StringRes int key) {
        return sDefaultValues.get(key);
    }

    public void putInt(@StringRes int key, int value) {
        doEdit();
        mEditor.putInt(mContext.getString(key), value);
        doCommit();
    }

    public void putString(@StringRes int key, String value) {
        doEdit();
        mEditor.putString(mContext.getString(key), value);
        doCommit();
    }

    public void putBoolean(@StringRes int key, boolean value) {
        doEdit();
        mEditor.putBoolean(mContext.getString(key), value);
        doCommit();
    }

    public void putFloat(@StringRes int key, float value) {
        doEdit();
        mEditor.putFloat(mContext.getString(key), value);
        doCommit();
    }

    public void putLong(@StringRes int key, long value) {
        doEdit();
        mEditor.putLong(mContext.getString(key), value);
        doCommit();
    }

    public int getInt(@StringRes int key) {
        return mPref.getInt(mContext.getString(key), (int) getDefaultValue(key));
    }

    public String getString(@StringRes int key) {
        return mPref.getString(mContext.getString(key), (String) getDefaultValue(key));
    }

    public boolean getBoolean(@StringRes int key) {
        return mPref.getBoolean(mContext.getString(key), (boolean) getDefaultValue(key));
    }

    public float getFloat(@StringRes int key) {
        return mPref.getFloat(mContext.getString(key), (float) getDefaultValue(key));
    }

    public long getLong(@StringRes int key) {
        return mPref.getLong(mContext.getString(key), (long) getDefaultValue(key));
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}
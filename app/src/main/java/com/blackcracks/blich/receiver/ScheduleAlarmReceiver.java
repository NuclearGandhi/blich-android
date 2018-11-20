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

package com.blackcracks.blich.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;
import com.firebase.jobdispatcher.JobService;

/**
 * A {@link JobService} that runs every few hours to update the database and notify
 * the user of any changes.
 */
public class ScheduleAlarmReceiver extends BroadcastReceiver {

    private AsyncTask<Void, Void, Void> mFetchBlichTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(final Context context, Intent intent) {
        PreferenceUtils.getInstance(context);
        mFetchBlichTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                RealmUtils.setUpRealm(context);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                BlichSyncTask.syncBlich(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Utilities.updateWidget(context);
            }
        };
        mFetchBlichTask.execute();
        ScheduleUtils.notifyUser(context);
    }
}

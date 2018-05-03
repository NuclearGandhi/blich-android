/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.Change;
import com.blackcracks.blich.data.DatedLesson;
import com.blackcracks.blich.data.Event;
import com.blackcracks.blich.data.Exam;
import com.blackcracks.blich.sync.BlichSyncTask;
import com.blackcracks.blich.util.PreferenceUtils;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;

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

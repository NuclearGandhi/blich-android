/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class BlichFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Integer> mFetchBlichTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchBlichTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                Context context = getApplicationContext();
                return BlichSyncTask.syncBlich(context);
            }

            @Override
            protected void onPostExecute(Integer status) {
                jobFinished(jobParameters, false);
                sendBroadcast(getApplicationContext(), status);
            }
        };

        mFetchBlichTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchBlichTask != null) {
            mFetchBlichTask.cancel(true);
        }
        return true;
    }


    private static void sendBroadcast(Context context, @BlichSyncTask.FetchStatus int status) {
        Intent intent = new Intent(Constants.IntentConstants.ACTION_SYNC_CALLBACK);
        intent.putExtra(Constants.IntentConstants.EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(context.getString(R.string.pref_fetch_status_key), status)
                .apply();
    }
}

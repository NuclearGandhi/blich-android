/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants;

public class BlichSyncIntentService extends IntentService {

    public BlichSyncIntentService() {
        super(BlichSyncIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int status = BlichSyncTask.syncBlich(getApplicationContext());
        sendBroadcast(getApplicationContext(), status);
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

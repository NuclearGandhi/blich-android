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
import com.blackcracks.blich.util.SyncUtils;
import com.blackcracks.blich.util.Utilities;

/**
 * An {@link IntentService} to run {@link BlichSyncTask#syncBlich(Context)} in the background.
 */
public class BlichSyncIntentService extends IntentService {

    public BlichSyncIntentService() {
        super(BlichSyncIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Begin sync and send its results.
        int status;
        if (Utilities.isThereNetworkConnection(getApplicationContext())) {
            status = BlichSyncTask.syncBlich(getApplicationContext());
        } else {
            status = SyncUtils.FETCH_STATUS_NO_CONNECTION;
        }
        sendBroadcast(getApplicationContext(), status);
        Utilities.updateWidget(getApplicationContext());
    }

    /**
     * Send a broadcast to all listeners that the sync has finished.
     *
     * @param status a {@link SyncUtils.FetchStatus} returned the from sync.
     */
    private static void sendBroadcast(Context context, @SyncUtils.FetchStatus int status) {
        Intent intent = new Intent(Constants.IntentConstants.ACTION_SYNC_CALLBACK);
        intent.putExtra(Constants.IntentConstants.EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(context.getString(R.string.pref_fetch_status_key), status)
                .apply();
    }
}

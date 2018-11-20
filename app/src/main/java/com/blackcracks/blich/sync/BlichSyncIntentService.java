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

package com.blackcracks.blich.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.blackcracks.blich.util.SyncCallbackUtils;
import com.blackcracks.blich.util.Utilities;

/**
 * An {@link IntentService} to run {@link BlichSyncTask#syncBlich(Context)} in the background.
 */
public class BlichSyncIntentService extends IntentService {

    public static final String ACTION_SYNC_FINISHED_CALLBACK = "sync_callback";
    public static final String EXTRA_FETCH_STATUS = "extra_fetch_status";

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
            status = SyncCallbackUtils.FETCH_STATUS_NO_CONNECTION;
        }
        sendBroadcast(getApplicationContext(), status);
        Utilities.updateWidget(getApplicationContext());
    }

    /**
     * Send a broadcast to all listeners that the sync has finished.
     *
     * @param status a {@link SyncCallbackUtils.FetchStatus} returned the from sync.
     */
    private static void sendBroadcast(Context context, @SyncCallbackUtils.FetchStatus int status) {
        Intent intent = new Intent(ACTION_SYNC_FINISHED_CALLBACK);
        intent.putExtra(EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }
}

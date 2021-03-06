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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.BlichSyncHelper;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A class containing utility methods to initialize sync and handle UI responses to it.
 */
public class SyncCallbackUtils {

    @Retention(SOURCE)
    @IntDef({FETCH_STATUS_SUCCESSFUL, FETCH_STATUS_UNSUCCESSFUL,
            FETCH_STATUS_NO_CONNECTION, FETCH_STATUS_CLASS_UNSUCCESSFUL})
    public @interface FetchStatus {
    }

    public static final int FETCH_STATUS_SUCCESSFUL = 0;
    public static final int FETCH_STATUS_UNSUCCESSFUL = 1;
    public static final int FETCH_STATUS_NO_CONNECTION = 2;

    public static final int FETCH_STATUS_CLASS_UNSUCCESSFUL = 101;

    /**
     * Initialize all the sync related settings and calls.
     */
    public static void initializeSync(Context context) {
        BlichSyncHelper.initializePeriodicSync(context);
        syncDatabase(context);
    }

    /**
     * Call an immediate sync. Cancel if sync is already taking place.
     */
    public static void syncDatabase(Context context) {
            PreferenceUtils.getInstance().putBoolean(R.string.pref_is_syncing_key, true);
            BlichSyncHelper.startImmediateSync(context);
    }

    /**
     * Callback for when sync finished.
     *
     * @param status          a {@link FetchStatus} returned from
     *                        the sync.
     */
    public static void syncFinishedCallback(FragmentActivity context,
                                            @FetchStatus int status,
                                            boolean dialogDismissible,
                                            final OnSyncRetryListener onRetryListener) {

        @StringRes int titleString;
        @StringRes int messageString;
        switch (status) {
            case FETCH_STATUS_SUCCESSFUL: {
                return;
            }
            case FETCH_STATUS_NO_CONNECTION: {
                titleString = R.string.dialog_fetch_no_connection_title;
                messageString = R.string.dialog_fetch_no_connection_message;
                break;
            }
            case FETCH_STATUS_UNSUCCESSFUL: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_unsuccessful_message;
                break;
            }
            case FETCH_STATUS_CLASS_UNSUCCESSFUL: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_class_unsuccessful_message;
                break;
            }
            default: {
                titleString = R.string.dialog_fetch_unsuccessful_title;
                messageString = R.string.dialog_fetch_unsuccessful_message;
                break;
            }
        }

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(titleString)
                .content(messageString)
                .positiveText(R.string.dialog_try_again)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onRetryListener.onRetry();
                    }
                });

        if (dialogDismissible) {
            dialogBuilder.negativeText(R.string.dialog_cancel);
        } else {
            dialogBuilder.cancelable(false);
        }

        MaterialDialog dialog = dialogBuilder.build();
        dialog.getView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        dialog.show();
    }

    /**
     * A listener for when sync callback fails.
     */
    public interface OnSyncRetryListener {
        /**
         * Called whenever a sync failed and it was requested to retry.
         */
        void onRetry();
    }
}

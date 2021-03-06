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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;
import com.blackcracks.blich.data.schedule.Period;
import com.blackcracks.blich.receiver.BootReceiver;
import com.blackcracks.blich.receiver.ScheduleAlarmReceiver;
import com.blackcracks.blich.util.PreferenceUtils;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

/**
 * A utility class for syncing.
 */
public class BlichSyncHelper {

    private static final int REQUEST_CODE_EVENING_ALARM = 0;
    private static final int REQUEST_CODE_MORNING_ALARM = 1;

    public static final String BLICH_SYNC_TAG = "blich_tag";

    /**
     * Start or cancel the periodic sync.
     */
    public static void initializePeriodicSync(@NonNull Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, ScheduleAlarmReceiver.class);
        PendingIntent eveningPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_EVENING_ALARM,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent morningPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MORNING_ALARM,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        boolean isNotificationsOn = PreferenceUtils.getInstance(context).getBoolean(R.string.pref_notification_toggle_key);
        if (isNotificationsOn) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            if (hourOfDay > 21 || (hourOfDay == 21 && minute > 30))
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 21);
            calendar.set(Calendar.MINUTE, 30);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    eveningPendingIntent
            );

            if (BuildConfig.DEBUG) {
                Timber.d("Evening sync starting on %s", calendar);
            }

            calendar = Calendar.getInstance();

            if (hourOfDay >= 7)
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    morningPendingIntent
            );

            if (BuildConfig.DEBUG) {
                Timber.d("Morning sync starting on %s", calendar);
            }
        } else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            alarmManager.cancel(eveningPendingIntent);
            alarmManager.cancel(morningPendingIntent);
        }

        if (BuildConfig.DEBUG) {
            Timber.d(isNotificationsOn ?
                        "Periodic sync is enabled" :
                        "Periodic sync is disabled");
        }
    }

    /**
     * Begin sync immediately.
     */
    public static void startImmediateSync(@NonNull Context context) {
        Intent intentToSyncImmediately = new Intent(context, BlichSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}

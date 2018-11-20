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

package com.blackcracks.blich.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.afollestad.appthemeengine.Config;
import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
import com.blackcracks.blich.util.ScheduleUtils;
import com.blackcracks.blich.util.Utilities;

import java.util.Calendar;
import java.util.Locale;

public class BlichWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager , int appWidgetId) {

        String ateKey = Utilities.getATEKey(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_schedule);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_schedule_title, pendingIntent);

        //Get the wanted day and display it on the toolbar.
        int day = ScheduleUtils.getWantedDayOfTheWeek();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day);

        Locale locale = new Locale("iw");
        views.setTextViewText(
                R.id.widget_schedule_title,
                "מערכת שעות - " + calendar.getDisplayName(
                        Calendar.DAY_OF_WEEK,
                        Calendar.LONG,
                        locale));

        int toolbarColor = Config.toolbarColor(context, ateKey, null);
        views.setInt(R.id.widget_schedule_title, "setBackgroundColor", toolbarColor);
        views.setTextColor(
                R.id.widget_schedule_title,
                Config.getToolbarTitleColor(context, null, ateKey, toolbarColor));

        int backgroundColor;
        if (ateKey.equals("light_theme")) {
            backgroundColor = ContextCompat.getColor(context, R.color.white_50);
        } else {
            backgroundColor = ContextCompat.getColor(context, R.color.grey_850);
        }
        views.setInt(R.id.widget_listview, "setBackgroundColor", backgroundColor);

        //Connect between the list and the adapter.
        intent = new Intent(context, ScheduleRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_listview, intent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

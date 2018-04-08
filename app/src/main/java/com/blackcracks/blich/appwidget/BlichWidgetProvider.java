/*
 * Written by Ido Fang Bentov
 * Copyright (C) Blich - All Rights Reserved
 */

package com.blackcracks.blich.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
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

        if (ateKey.equals("dark_theme")) {
            int color = ContextCompat.getColor(context, R.color.grey_850);
            views.setInt(R.id.widget_listview, "setBackgroundColor", color);
        }
        //Connect between the list and the adapter.
        intent = new Intent(context, ScheduleRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_listview, intent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

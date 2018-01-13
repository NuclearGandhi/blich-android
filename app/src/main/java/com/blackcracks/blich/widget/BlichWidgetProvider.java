package com.blackcracks.blich.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;
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

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_schedule_title, pendingIntent);

        int day = Utilities.Schedule.getWantedDayOfTheWeek();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day);

        Locale locale = new Locale("iw");
        views.setTextViewText(
                R.id.widget_schedule_title,
                "מערכת שעות - " + calendar.getDisplayName(
                        Calendar.DAY_OF_WEEK,
                        Calendar.LONG,
                        locale));
        views.setTextColor(R.id.widget_schedule_title, Color.WHITE);

        intent = new Intent(context, ScheduleRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_listview, intent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

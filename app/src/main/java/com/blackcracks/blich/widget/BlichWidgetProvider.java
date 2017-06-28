package com.blackcracks.blich.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.blackcracks.blich.R;
import com.blackcracks.blich.activity.MainActivity;

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

        intent = new Intent(context, ScheduleRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_listview, intent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

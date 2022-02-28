package com.example.holdsafety;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class HoldSafetyWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId: appWidgetIds){ //user can put multiple widgets
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("isFromWidget", "true");

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_holdsafety);
            views.setOnClickPendingIntent(R.id.btnSafetyButtonWidget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}

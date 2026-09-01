package com.yeop.timelogger;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeWidget extends AppWidgetProvider {
    private static final String ACTION_START = "com.yeop.timelogger.START";
    private static final String ACTION_END = "com.yeop.timelogger.END";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) updateWidget(context, appWidgetManager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            TimeLogStore.add(context, "START");
            vibrate(context);
            refreshAll(context);
        } else if (ACTION_END.equals(action)) {
            TimeLogStore.add(context, "END");
            vibrate(context);
            refreshAll(context);
        }
    }

    private static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TimeWidget.class));
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_time);

        Intent start = new Intent(context, TimeWidget.class).setAction(ACTION_START);
        Intent end = new Intent(context, TimeWidget.class).setAction(ACTION_END);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        views.setOnClickPendingIntent(R.id.widgetStart,
                PendingIntent.getBroadcast(context, 101, start, flags));
        views.setOnClickPendingIntent(R.id.widgetEnd,
                PendingIntent.getBroadcast(context, 102, end, flags));

        List<TimeLogStore.Record> records = TimeLogStore.getAll(context);
        String last = "마지막 기록 없음";
        if (!records.isEmpty()) {
            TimeLogStore.Record r = records.get(records.size() - 1);
            String label = "START".equals(r.type) ? "시작" : "끝";
            String t = new SimpleDateFormat("HH:mm", Locale.KOREA).format(new Date(r.time));
            last = label + " " + t;
        }
        views.setTextViewText(R.id.widgetLast, last);
        manager.updateAppWidget(appWidgetId, views);
    }

    private static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(45);
        }
    }
}

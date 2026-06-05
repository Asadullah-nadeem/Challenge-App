package com.example.challengeapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

public class DaysLeftWidget extends AppWidgetProvider {

    public static final String ACTION_AUTO_UPDATE = "com.example.challengeapp.AUTO_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (ACTION_AUTO_UPDATE.equals(intent.getAction()) || 
            Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
            
            sendMidnightNotification(context);
        }

        if (ACTION_AUTO_UPDATE.equals(intent.getAction()) || 
            Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, DaysLeftWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void sendMidnightNotification(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        int daysLeft = calculateDaysLeftInYear();
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHALLENGE_APP_CHANNEL")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(daysLeft + " Days Left")
                .setContentText("A new day has begun! Make every day count.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleNextUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleNextUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelUpdate(context);
    }

    private static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DaysLeftWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);

        // Schedule for next midnight
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1); // 1 minute past midnight
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private static void cancelUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DaysLeftWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        int daysLeft = calculateDaysLeftInYear();
        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widgetTvDaysLeft, String.valueOf(daysLeft));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int calculateDaysLeftInYear() {
        Calendar today = Calendar.getInstance();
        Calendar endOfYear = Calendar.getInstance();
        
        int currentYear = today.get(Calendar.YEAR);
        endOfYear.set(Calendar.YEAR, currentYear);
        endOfYear.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfYear.set(Calendar.DAY_OF_MONTH, 31);
        
        endOfYear.set(Calendar.HOUR_OF_DAY, 23);
        endOfYear.set(Calendar.MINUTE, 59);
        endOfYear.set(Calendar.SECOND, 59);
        endOfYear.set(Calendar.MILLISECOND, 999);

        long diffInMillis = endOfYear.getTimeInMillis() - today.getTimeInMillis();
        if (diffInMillis < 0) diffInMillis = 0;
        return (int) (diffInMillis / (1000 * 60 * 60 * 24));
    }
}

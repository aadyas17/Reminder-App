package com.example.n1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String task = intent.getStringExtra("task");
        int notificationType = intent.getIntExtra("notificationType", 0);

        // Create appropriate notification text
        String notificationText;
        if (notificationType == 1) {
            notificationText = "Reminder: " + task + " (5 minutes remaining)";
        } else {
            notificationText = "Reminder: " + task;
        }

        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "taskReminderChannel")
                .setSmallIcon(R.drawable.img_3)
                .setContentTitle("Task Reminder")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Intent for notification tap action
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationType, builder.build());
        }
    }
}

package com.reynem.tamemind.utils;

import android.app.NotificationChannel;
import android.content.Context;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.util.Log;

import com.reynem.tamemind.R;

public class NotificationFarm {
    public void showNotification(Context context, String title, String message){
        String channelId = "farm_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Farm Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.tamemind_ico)
                .setContentTitle(title)
                .setContentText(message)
                .build();

        notificationManager.notify(1, notification);

        Log.d("Success", "Notification was sent");
    }
}

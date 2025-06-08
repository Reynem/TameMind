package com.reynem.tamemind.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.reynem.tamemind.R;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.utils.TimerConstants;

public class TimerNotificationService extends Service {
    private static final String CHANNEL_ID = TimerConstants.TIMER_NOTIFICATION_CHANNEL_ID;
    private static final int NOTIFICATION_ID = TimerConstants.TIMER_NOTIFICATION_ID;
    private static final String TAG = "TimerNotificationService";

    private Handler handler;
    private Runnable updateRunnable;
    private NotificationManager notificationManager;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        handler = new Handler();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (!isRunning) {
            startTimerNotification();
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        isRunning = false;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Timer Notifications",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Shows remaining timer time");
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
        Log.d(TAG, "Notification channel created");
    }

    private void startTimerNotification() {
        Log.d(TAG, "Starting timer notification");

        updateNotificationNow();

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
                long blockUntil = prefs.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
                long currentTime = System.currentTimeMillis();
                long timeRemaining = blockUntil - currentTime;

                Log.d(TAG, "Block until: " + blockUntil + ", Current: " + currentTime + ", Remaining: " + timeRemaining);

                if (timeRemaining > 0) {
                    int minutes = (int) (timeRemaining / (60 * 1000));
                    int seconds = (int) ((timeRemaining / 1000) % 60);

                    String timeText;
                    if (seconds < 10) {
                        timeText = minutes + ":0" + seconds;
                    } else {
                        timeText = minutes + ":" + seconds;
                    }

                    updateNotification(timeText);
                    handler.postDelayed(this, 1000);
                } else {
                    Log.d(TAG, "Timer ended, stopping service");
                    stopSelf();
                }
            }
        };

        handler.postDelayed(updateRunnable, 1000);
    }

    private void updateNotificationNow() {
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long blockUntil = prefs.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();
        long timeRemaining = blockUntil - currentTime;

        if (timeRemaining > 0) {
            int minutes = (int) (timeRemaining / (60 * 1000));
            int seconds = (int) ((timeRemaining / 1000) % 60);

            String timeText;
            if (seconds < 10) {
                timeText = minutes + ":0" + seconds;
            } else {
                timeText = minutes + ":" + seconds;
            }

            updateNotification(timeText);
        }
    }

    private void updateNotification(String timeText) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Focus Session Active")
                .setContentText("Time remaining: " + timeText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();

        try {
            startForeground(NOTIFICATION_ID, notification);
            Log.d(TAG, "Notification updated: " + timeText);
        } catch (Exception e) {
            Log.e(TAG, "Failed to update notification", e);
        }
    }
}
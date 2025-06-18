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
import com.reynem.tamemind.history.HistoryManager;
import com.reynem.tamemind.utils.NotificationFarm;

import java.util.Locale;

public class TimerNotificationService extends Service {
    private static final String CHANNEL_ID = TimerConstants.TIMER_NOTIFICATION_CHANNEL_ID;
    private static final int NOTIFICATION_ID = TimerConstants.TIMER_NOTIFICATION_ID;
    private static final String TAG = "TimerNotificationService";

    private Handler handler;
    private Runnable updateRunnable;
    private NotificationManager notificationManager;
    private boolean isRunning = false;
    private SharedPreferences sharedPreferences;

    private HistoryManager historyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        handler = new Handler();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        historyManager = new HistoryManager(this);
        sharedPreferences = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (!isRunning) {
            long blockUntil = sharedPreferences.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
            if (blockUntil > System.currentTimeMillis()) {
                startTimerNotification();
                isRunning = true;
            } else {
                Log.d(TAG, "No active timer found, stopping service");
                stopSelf();
            }
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
                long blockUntil = sharedPreferences.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
                long currentTime = System.currentTimeMillis();
                long timeRemaining = blockUntil - currentTime;

                if (timeRemaining > 1000) {
                    int totalSeconds = (int) (timeRemaining / 1000);
                    int minutes = totalSeconds / 60;
                    int seconds = totalSeconds % 60;

                    String timeText = String.format(Locale.US, "%d:%02d", minutes, seconds);
                    updateNotification(timeText);

                    handler.postDelayed(this, 1000);
                } else {
                    Log.d(TAG, "Timer completed naturally");
                    handleTimerCompletion();
                }
            }
        };

        handler.postDelayed(updateRunnable, 1000);
    }

    private void updateNotificationNow() {
        long blockUntil = sharedPreferences.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();
        long timeRemaining = blockUntil - currentTime;

        if (timeRemaining > 0) {
            int totalSeconds = (int) (timeRemaining / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            String timeText = String.format(Locale.US, "%d:%02d", minutes, seconds);
            updateNotification(timeText);
        } else {
            handleTimerCompletion();
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
                .setSmallIcon(R.drawable.tamemind_ico)
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

    private void handleTimerCompletion() {
        Log.d(TAG, "Processing timer completion");

        long sessionStartTime = sharedPreferences.getLong(TimerConstants.PREF_TIMER_START_TIME, 0);
        float lastTimerValue = sharedPreferences.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 0);
        int originalDurationMinutes = (int) lastTimerValue;

        if (sessionStartTime > 0) {
            historyManager.saveCompletedSession(
                    sessionStartTime,
                    System.currentTimeMillis(),
                    originalDurationMinutes
            );

            int coinsEarned = calculateCoinsForSession(originalDurationMinutes);
            addCoins(coinsEarned);

            sendSuccessNotification(coinsEarned);

            Log.d(TAG, "Session completed successfully. Coins earned: " + coinsEarned);
        } else {
            Log.w(TAG, "No start time found for session");
        }

        clearBlockTime();

        stopSelf();
    }

    private int getCoins() {
        return sharedPreferences.getInt(TimerConstants.PREF_KEY_COINS, 0);
    }

    private void saveCoins(int coins) {
        sharedPreferences.edit()
                .putInt(TimerConstants.PREF_KEY_COINS, Math.max(0, coins))
                .apply();
    }

    private void addCoins(int amount) {
        int currentCoins = getCoins();
        int newCoins = currentCoins + amount;
        saveCoins(newCoins);
        Log.d(TAG, "Coins added: " + amount + ", total: " + newCoins);
    }

    private int calculateCoinsForSession(int minutes) {
        int baseCoins = (minutes / 5) * 100;

        int bonus = 0;
        if (minutes >= 60) {
            bonus = 500;
        } else if (minutes >= 45) {
            bonus = 300;
        } else if (minutes >= 30) {
            bonus = 200;
        } else if (minutes >= 15) {
            bonus = 100;
        }

        int totalCoins = baseCoins + bonus;
        Log.d(TAG, "Calculated coins for " + minutes + " minutes: " + totalCoins + " (base: " + baseCoins + ", bonus: " + bonus + ")");

        return totalCoins;
    }

    private void sendSuccessNotification(int coinsEarned) {
        try {
            NotificationFarm notificationFarm = new NotificationFarm();
            String message = getString(R.string.session_completed_with_coins, coinsEarned);
            notificationFarm.showNotification(this, getString(R.string.success), message);
            Log.d(TAG, "Success notification sent");
        } catch (Exception e) {
            Log.e(TAG, "Failed to send success notification", e);
        }
    }

    private void clearBlockTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TimerConstants.PREF_BLOCK_UNTIL);
        editor.remove(TimerConstants.PREF_TIMER_START_TIME);
        editor.apply();
        Log.d(TAG, "Block time cleared");
    }
}
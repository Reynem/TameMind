package com.reynem.tamemind.blocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.reynem.tamemind.R;
import com.reynem.tamemind.utils.TimerConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

public class AppBlockerService extends Service {

    // TODO: CHANGE STRICT MODE TO OVERLAY

    private static final String TAG = "AppBlockerService";
    private static final long POLL_INTERVAL = 1000; // ms
    private static final String CHANNEL_ID = "app_blocker_channel";
    private static final int NOTIFICATION_ID = 1001;

    private final Handler handler = new Handler();
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            checkForegroundApp();
            handler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(pollRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Restart service if needed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollRunnable);
    }

    private void checkForegroundApp() {
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long blockUntil = prefs.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime < blockUntil) {
            String foregroundApp = getForegroundApp();
            if (foregroundApp != null) {
                Set<String> allowedApps = prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, getDefaultAllowedApps());
                if (!allowedApps.contains(foregroundApp)) {
                    Log.d(TAG, "Foreground app: " + foregroundApp);
                    showBlockNotification();
                }
            }
        }
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 10, now);

        if (stats == null || stats.isEmpty()) return null;

        UsageStats recent = null;
        for (UsageStats stat : stats) {
            if (stat.getLastTimeUsed() > (recent == null ? 0 : recent.getLastTimeUsed())) {
                recent = stat;
            }
        }

        return recent != null ? recent.getPackageName() : null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "App Blocker",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for blocked apps");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void showBlockNotification() {
        createNotificationChannel();

        Intent fullScreenIntent = new Intent(this, BlockedActivity.class);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this, 0, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock)
                .setContentTitle("App Blocked")
                .setContentText("This app is currently blocked")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void addAllowedApp(SharedPreferences prefs, String packageName) {
        Set<String> allowedApps = new HashSet<>(prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, getDefaultAllowedApps()));
        allowedApps.add(packageName);
        prefs.edit().putStringSet(TimerConstants.PREF_ALLOWED_APPS, allowedApps).apply();
    }

    public static void removeAllowedApp(SharedPreferences prefs, String packageName) {
        Set<String> allowedApps = new HashSet<>(prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, getDefaultAllowedApps()));
        allowedApps.remove(packageName);
        prefs.edit().putStringSet(TimerConstants.PREF_ALLOWED_APPS, allowedApps).apply();
    }

    public static Set<String> getDefaultAllowedApps() {
        Set<String> defaultApps = new HashSet<>();
        defaultApps.add("com.reynem.tamemind");
        defaultApps.add("com.android.settings");
        defaultApps.add("com.android.launcher");
        defaultApps.add("com.google.android.apps.nexuslauncher");
        return defaultApps;
    }
}
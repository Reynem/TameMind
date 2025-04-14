package com.reynem.tamemind.blocker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class AppBlockerService extends AccessibilityService {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_ALLOWED_APPS = "allowed_apps";
    private static final String KEY_BLOCK_UNTIL = "block_until";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long blockUntil = prefs.getLong(KEY_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();

        if (event.getPackageName() != null) {
            String packageName = event.getPackageName().toString();
            String myPackageName = getApplicationContext().getPackageName();

            if (currentTime >= blockUntil) {
                Log.d("AppBlockerService", "Blocking is disabled (time)");
                return;
            }

            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Set<String> allowedApps = prefs.getStringSet(KEY_ALLOWED_APPS, getDefaultAllowedApps());

                if (!allowedApps.contains(packageName)) {
                    showBlockScreen();
                } else {
                    Log.d("AppBlockerService", "Skipping block for allowed app: " + packageName);
                }
            }
        }
    }

    private void showBlockScreen() {
        Intent intent = new Intent(this, BlockedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show();
    }

    public static void addAllowedApp(SharedPreferences prefs, String packageName) {
        Set<String> allowedApps = new HashSet<>(prefs.getStringSet(KEY_ALLOWED_APPS, getDefaultAllowedApps()));
        allowedApps.add(packageName);
        prefs.edit().putStringSet(KEY_ALLOWED_APPS, allowedApps).apply();
    }

    public static void removeAllowedApp(SharedPreferences prefs, String packageName) {
        Set<String> allowedApps = new HashSet<>(prefs.getStringSet(KEY_ALLOWED_APPS, getDefaultAllowedApps()));
        allowedApps.remove(packageName);
        prefs.edit().putStringSet(KEY_ALLOWED_APPS, allowedApps).apply();
    }

    public static Set<String> getDefaultAllowedApps() {
        Set<String> defaultApps = new HashSet<>();
        defaultApps.add("com.reynem.tamemind"); // My application
        defaultApps.add("com.android.settings"); // Settings
        defaultApps.add("com.android.launcher"); // Standart launcher
        defaultApps.add("com.google.android.apps.nexuslauncher"); // Emulator`s launcher
        return defaultApps;
    }
}

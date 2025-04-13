package com.reynem.tamemind.blocker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AppBlockerService extends AccessibilityService {
    private static final String HOME_PACKAGE_NAME = "com.android.launcher";
    private static final String HOME_PACKAGE_NAME_NEXUS = "com.google.android.apps.nexuslauncher";

    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        long blockUntil = prefs.getLong("block_until", 0);
        long currentTime = System.currentTimeMillis();

        Log.d("AppBlockerService", "Accessibility Event: " + event.getEventType());
        if (event.getPackageName() != null) {
            String packageName = event.getPackageName().toString();
            Log.d("AppBlockerService", "Package Name: " + packageName);

            String myPackageName = getApplicationContext().getPackageName();

            Log.d("AppBlockerService", "Current Time: " + currentTime);
            Log.d("AppBlockerService", "Block Until: " + blockUntil);

            if (currentTime >= blockUntil) {
                Log.d("AppBlockerService", "Blocking is disabled (time)");
                return;
            }

            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Log.d("AppBlockerService", "Window state changed for: " + packageName);

                // Проверяем, не является ли запущенное приложение TameMind или настройками
                if (!packageName.equals(myPackageName) && !packageName.equals(SETTINGS_PACKAGE_NAME)
                        && !packageName.equals(HOME_PACKAGE_NAME) && !packageName.equals(HOME_PACKAGE_NAME_NEXUS)) {
                    showBlockScreen();
                } else {
                    Log.d("AppBlockerService", "Skipping block for: " + packageName);
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
}
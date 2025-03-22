package com.reynem.tamemind.blocker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AppBlockerService extends AccessibilityService {
    private static final String BLOCKED_APP = "com.google.android.youtube"; // temporary constant

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        long blockUntil = prefs.getLong("block_until", 0);

        if (System.currentTimeMillis() >= blockUntil) {
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            if (event.getPackageName() != null && packageName.equals(BLOCKED_APP)) {
                showBlockScreen();
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

package com.reynem.tamemind.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.Settings;

import me.tankery.lib.circularseekbar.CircularSeekBar;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.services.TimerNotificationService;
import com.reynem.tamemind.settings.SettingsActivity;
import com.reynem.tamemind.utils.NotificationFarm;
import com.reynem.tamemind.utils.TimerConstants;

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private float progress;
    private long lastClickTime = 0;
    private TextView shownTime, motivationTextView;
    private Button startTimer, endTimer;
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;
    private Runnable timerRunnable;
    private boolean isTimerActive = false;
    private final MotivationMessages motivationMessages = new MotivationMessages();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // I will put it there
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        if (!isAccessibilityServiceEnabled(this, AppBlockerService.class)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        NavigationView navigationView = findViewById(R.id.navigationMenu);

        navigationManager = new NavigationManager(navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        View headerView = navigationView.getHeaderView(0);
        ImageView closeButton = headerView.findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_farm) {
                startActivity(new Intent(this, FarmActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_home;
        });

        motivationTextView = findViewById(R.id.motivation);
        Resources appResources = getResources();

        // Initialization of list of messages
        motivationMessages.initializeMotivationMessages(appResources);
        motivationMessages.updateMotivationMessage(motivationTextView);

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        startTimer = findViewById(R.id.startTimer);
        endTimer = findViewById(R.id.endTimer);
        shownTime = findViewById(R.id.timeLeft);

        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                assert circularSeekBar != null;
                progress = circularSeekBar.getProgress();
                if (progress % 5 != 0) progress -= (progress % 5);
                circularSeekBar.setProgress(progress);
                shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", (int) progress, 0));

                SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putFloat(TimerConstants.PREF_LAST_TIMER_VALUE, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {

            }
        });

        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        float lastTimerValue = prefs.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 25); // 25 by default
        circularSeekBar.setProgress(lastTimerValue);
        shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", (int) lastTimerValue, 0));

        endTimer.setOnClickListener(v -> {
            finishTimer(circularSeekBar, false);
            progress = 0;
        });

        startTimer.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) return;
            lastClickTime = currentTime;

            if (isTimerActive) return;

            progress = circularSeekBar.getProgress() * 60;
            shownTime = findViewById(R.id.timeLeft);
            startTimer.setVisibility(View.INVISIBLE);
            endTimer.setVisibility(View.VISIBLE);
            startCountdown(circularSeekBar);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        motivationMessages.updateMotivationMessage(motivationTextView);
    }

    private void startCountdown(CircularSeekBar circularSeekBar) {
        if (isTimerActive) {
            handler.removeCallbacks(timerRunnable);
        }
        isTimerActive = true;

        int minutes = (int) (progress / 60);
        setBlockTime(minutes);
        Intent timerServiceIntent = new Intent(this, TimerNotificationService.class);
        startService(timerServiceIntent);

        circularSeekBar.setDisablePointer(true);
        startTimer.setVisibility(View.INVISIBLE);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (progress > 0) {
                    progress--;
                    int minutes = (int) (progress / 60);
                    int seconds = (int) (progress % 60);
                    circularSeekBar.setProgress(minutes);
                    shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

                    handler.postDelayed(this, 1000);
                } else {
                    finishTimer(circularSeekBar, true);
                }
            }
        };

        handler.postDelayed(timerRunnable, 1000);
    }

    private void finishTimer(CircularSeekBar circularSeekBar, boolean shouldSendNotification) {
        isTimerActive = false;
        clearBlockTime();
        Intent timerServiceIntent = new Intent(this, TimerNotificationService.class);
        stopService(timerServiceIntent);
        circularSeekBar.setDisablePointer(false);
        startTimer.setVisibility(View.VISIBLE);
        endTimer.setVisibility(View.INVISIBLE);
        if (shouldSendNotification) sendSuccessNotification();

        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        float lastTimerValue = prefs.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 25); // 25 by default
        circularSeekBar.setProgress(lastTimerValue);
    }

    private void setBlockTime(int minutes) {
        long blockUntil = System.currentTimeMillis() + ((long) minutes * 60 * 1000);
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(TimerConstants.PREF_BLOCK_UNTIL, blockUntil).apply();
        long allTime = prefs.getLong(TimerConstants.PREF_GET_ALL_TIME, 0L);
        allTime += blockUntil;
        prefs.edit().putLong(TimerConstants.PREF_GET_ALL_TIME, allTime).apply();
    }

    private void clearBlockTime() {
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(TimerConstants.PREF_BLOCK_UNTIL).apply();
    }


    private void sendSuccessNotification(){
        NotificationFarm notificationFarm = new NotificationFarm();
        notificationFarm.showNotification(this, getString(R.string.success), getString(R.string.the_end_of_feeding_process));
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityServiceClass) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityServiceClass);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return false;
        }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(TimerConstants.PREF_PROGRESS_SECONDS, progress);
        outState.putBoolean(TimerConstants.PREF_IS_TIMER_ACTIVE, isTimerActive);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        progress = savedInstanceState.getFloat(TimerConstants.PREF_PROGRESS_SECONDS);
        isTimerActive = savedInstanceState.getBoolean(TimerConstants.PREF_IS_TIMER_ACTIVE);

        if (isTimerActive) {
            CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
            int minutes = (int) (progress / 60);
            int seconds = (int) (progress % 60);
            circularSeekBar.setProgress(minutes);
            startTimer.setVisibility(View.INVISIBLE);
            endTimer.setVisibility(View.VISIBLE);
            circularSeekBar.setDisablePointer(true);
            shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            startCountdown(circularSeekBar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}
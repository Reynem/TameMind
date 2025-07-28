package com.reynem.tamemind.main;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.Toast;

import me.tankery.lib.circularseekbar.CircularSeekBar;
import java.util.Locale;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.services.TimerNotificationService;
import com.reynem.tamemind.settings.SettingsActivity;
import com.reynem.tamemind.shop.ShopActivity;
import com.reynem.tamemind.utils.CoinsManager;
import com.reynem.tamemind.utils.NotificationFarm;
import com.reynem.tamemind.utils.TimerConstants;
import com.reynem.tamemind.utils.TimerSyncHelper;
import com.reynem.tamemind.history.HistoryManager;
import com.reynem.tamemind.history.HistoryActivity;

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    private long lastClickTime = 0;
    private long timerStartTime = 0;
    private TextView shownTime, motivationTextView, coinsDisplay;
    private Button startTimer, endTimer;
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;
    private Runnable timerRunnable;
    private boolean isTimerActive = false;
    private final MotivationMessages motivationMessages = new MotivationMessages();
    private HistoryManager historyManager;
    private CoinsManager coinsManager;
    private TimerSyncHelper timerSyncHelper;

    private SharedPreferences sharedPreferences;

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

        if (!isUsageStatsPermissionGranted(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, getString(R.string.please_allow_access_usm),
                    Toast.LENGTH_LONG).show();
        }

        startService(new Intent(this, AppBlockerService.class));

        sharedPreferences = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        coinsDisplay = findViewById(R.id.coinsAmount);
        coinsManager = new CoinsManager(this);
        timerSyncHelper = new TimerSyncHelper(this);

        checkNotificationPermission();

        // I will put it there

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
            } else if (id == R.id.nav_history){
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else if (id == R.id.nav_shop) {
                startActivity(new Intent(this, ShopActivity.class));
                return true;
            } else return id == R.id.nav_home;
        });

        motivationTextView = findViewById(R.id.motivation);
        Resources appResources = getResources();

        // Initialization of list of messages
        motivationMessages.initializeMotivationMessages(appResources);
        motivationMessages.updateMotivationMessage(motivationTextView);

        historyManager = new HistoryManager(this);

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        startTimer = findViewById(R.id.startTimer);
        endTimer = findViewById(R.id.endTimer);
        shownTime = findViewById(R.id.timeLeft);

        coinsManager.updateCoinsDisplay(coinsDisplay);

        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                assert circularSeekBar != null;
                float progress = circularSeekBar.getProgress();
                if (progress % 5 != 0) progress -= (progress % 5);
                circularSeekBar.setProgress(progress);
                shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", (int) progress, 0));

                sharedPreferences.edit().putFloat(TimerConstants.PREF_LAST_TIMER_VALUE, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {

            }
        });

        float lastTimerValue = sharedPreferences.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 25); // 25 by default
        circularSeekBar.setProgress(lastTimerValue);
        shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", (int) lastTimerValue, 0));

        checkForActiveTimer(circularSeekBar);

        endTimer.setOnClickListener(v -> finishTimer(circularSeekBar));

        startTimer.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) return;
            lastClickTime = currentTime;

            if (isTimerActive) return;

            float timerMinutes = circularSeekBar.getProgress();
            startCountdown(circularSeekBar, timerMinutes);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        motivationMessages.updateMotivationMessage(motivationTextView);

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        checkForActiveTimer(circularSeekBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinsManager.updateCoinsDisplay(coinsDisplay);

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        checkForActiveTimer(circularSeekBar);
    }

    /**
     * Checks if there is an active timer and synchronizes the UI
     */
    private void checkForActiveTimer(CircularSeekBar circularSeekBar) {
        TimerSyncHelper.TimerState timerState = timerSyncHelper.getTimerState();

        if (timerState.isActive) {
            if (!isTimerActive) {
                timerStartTime = timerState.startTime;
                isTimerActive = true;
                startTimer.setVisibility(View.INVISIBLE);
                if (!sharedPreferences.getBoolean(TimerConstants.PREF_SELECTED_MODE, false)){
                    endTimer.setVisibility(View.VISIBLE);
                }
                circularSeekBar.setDisablePointer(true);

                startSynchronizedTimer(circularSeekBar);
            }
        } else {
            if (isTimerActive) {
                finishTimerSilently(circularSeekBar);
            }
        }
    }

    private void startCountdown(CircularSeekBar circularSeekBar, float minutes) {
        if (isTimerActive) {
            handler.removeCallbacks(timerRunnable);
        }

        isTimerActive = true;
        timerStartTime = System.currentTimeMillis();
        timerSyncHelper.setBlockTime((int) minutes);

        Intent timerServiceIntent = new Intent(this, TimerNotificationService.class);
        startForegroundService(timerServiceIntent);

        circularSeekBar.setDisablePointer(true);
        startTimer.setVisibility(View.INVISIBLE);
        if (!sharedPreferences.getBoolean(TimerConstants.PREF_SELECTED_MODE, false)){
            endTimer.setVisibility(View.VISIBLE);
        }

        startSynchronizedTimer(circularSeekBar);
    }

    /**
     * Starts a timer synchronized with SharedPreferences
     */
    private void startSynchronizedTimer(CircularSeekBar circularSeekBar) {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                TimerSyncHelper.TimerTime remainingTime = timerSyncHelper.getRemainingMinutesAndSeconds();

                if (timerSyncHelper.isTimerActive()) {
                    int displayMinutes = Math.max(0, remainingTime.minutes + (remainingTime.seconds > 0 ? 1 : 0));
                    circularSeekBar.setProgress(displayMinutes);
                    shownTime.setText(remainingTime.getFormattedTime());

                    handler.postDelayed(this, 1000);
                } else {
                    finishTimer(circularSeekBar);
                }
            }
        };
        handler.postDelayed(timerRunnable, 100);
    }

    private void finishTimer(CircularSeekBar circularSeekBar) {
        boolean wasNotEarlyStopped = false;

        if (isTimerActive && timerStartTime > 0) {
            wasNotEarlyStopped = timerSyncHelper.wasStoppedEarly();
        }

        finishTimerUI(circularSeekBar);

        if (wasNotEarlyStopped) {
            android.util.Log.d("MainActivity", "Session was stopped early");
            int coinsPenalty = coinsManager.calculatePenaltyCoins();
            coinsManager.removeCoins(coinsPenalty);
            sendPenaltyNotification(coinsPenalty);

            applyEarlyStopPenalty();
        }

        coinsManager.updateCoinsDisplay(coinsDisplay);
    }

    /**
     * Finishes the timer without applying penalties (for cases where the timer finished naturally)
     */
    private void finishTimerSilently(CircularSeekBar circularSeekBar) {
        finishTimerUI(circularSeekBar);
        coinsManager.updateCoinsDisplay(coinsDisplay);
    }

    /**
     * Updates the UI after the timer finishes
     */
    private void finishTimerUI(CircularSeekBar circularSeekBar) {
        isTimerActive = false;
        timerStartTime = 0;

        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        timerSyncHelper.clearBlockTime();
        Intent timerServiceIntent = new Intent(this, TimerNotificationService.class);
        stopService(timerServiceIntent);

        circularSeekBar.setDisablePointer(false);
        startTimer.setVisibility(View.VISIBLE);
        endTimer.setVisibility(View.INVISIBLE);

        float lastTimerValue = sharedPreferences.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 25);
        circularSeekBar.setProgress(lastTimerValue);
        shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", (int) lastTimerValue, 0));
    }

    private void applyEarlyStopPenalty() {
        long allTime = sharedPreferences.getLong(TimerConstants.PREF_GET_ALL_TIME, 0L);
        long newAllTime = allTime / 2;
        sharedPreferences.edit().putLong(TimerConstants.PREF_GET_ALL_TIME, newAllTime).apply();
    }

    private void setBlockTime(int minutes) {
        timerSyncHelper.setBlockTime(minutes);
    }

    private void clearBlockTime() {
        timerSyncHelper.clearBlockTime();
    }

    private void sendPenaltyNotification(int coinsLost) {
        NotificationFarm notificationFarm = new NotificationFarm();
        String message = getString(R.string.penalty_with_coins, coinsLost);
        notificationFarm.showNotification(this, getString(R.string.penalty), message);
    }

    public static boolean isUsageStatsPermissionGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission for notifications")
                            .setMessage("To work the timer, you need permission to show notifications")
                            .setPositiveButton("Enable", (dialog, which) -> ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1))
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TimerConstants.PREF_IS_TIMER_ACTIVE, isTimerActive);
        outState.putLong("TIMER_START_TIME", timerStartTime);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isTimerActive = savedInstanceState.getBoolean(TimerConstants.PREF_IS_TIMER_ACTIVE);
        timerStartTime = savedInstanceState.getLong("TIMER_START_TIME", 0);

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        checkForActiveTimer(circularSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}
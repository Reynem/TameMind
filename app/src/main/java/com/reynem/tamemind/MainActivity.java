package com.reynem.tamemind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.tankery.lib.circularseekbar.CircularSeekBar;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;
import com.reynem.tamemind.utils.NotificationFarm;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationListener {
    private final Handler handler = new Handler();
    private float progress;
    private TextView shownTime;
    private Button startTimer, endTimer;
    private NavigationManager navigationManager;
    private Runnable timerRunnable;
    private boolean isTimerActive = false;

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

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        navigationManager = new NavigationManager(navigationView);
        navigationManager.hideNavigationView();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home){

                return true;
            }
            else if (id == R.id.nav_farm){
                Intent intent1 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent1);
                return true;
            }
            return false;
        });

        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> {
            navigationView.setVisibility(NavigationView.VISIBLE);
            showNavigationView();
        });

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
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {

            }
        });

        endTimer.setOnClickListener(v -> {
            finishTimer(circularSeekBar);
            progress = 0;
        });

        startTimer.setOnClickListener(v -> {
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

        TextView motivationMessage = findViewById(R.id.motivation);
        if (motivationMessage != null) {
            ArrayList<String> sampleMessages = new ArrayList<>();
            sampleMessages.add("Stop phubbing");
            sampleMessages.add("Return to your work");
            sampleMessages.add("Do not use your phone!");
            sampleMessages.add("Put your phone down!");
            sampleMessages.add("Focus on your tasks!");
            sampleMessages.add("Stay productive!");
            sampleMessages.add("Break the phone habit!");
            sampleMessages.add("Time to work, not scroll!");
            sampleMessages.add("Don't let your phone distract you!");
            sampleMessages.add("Stay focused, stay sharp!");
            sampleMessages.add("Your work needs you more!");
            sampleMessages.add("Eyes on your goals, not your screen!");
            sampleMessages.add("Be present, not distracted!");

            motivationMessage.setText(sampleMessages.get((int) (Math.random() * 12)));
        }
         else {
            android.util.Log.e("MainActivity", "TextView motivation not found in layout.");
        }

    }

    private void startCountdown(CircularSeekBar circularSeekBar) {
        if (isTimerActive) {
            handler.removeCallbacks(timerRunnable);
        }
        isTimerActive = true;

        int minutes = (int) (progress / 60);
        setBlockTime(minutes);

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
                    finishTimer(circularSeekBar);
                }
            }
        };

        handler.postDelayed(timerRunnable, 1000);
    }

    private void finishTimer(CircularSeekBar circularSeekBar) {
        isTimerActive = false;
        clearBlockTime();
        circularSeekBar.setDisablePointer(false);
        startTimer.setVisibility(View.VISIBLE);
        sendSuccessNotification();
    }

    private void setBlockTime(int minutes) {
        long blockUntil = System.currentTimeMillis() + ((long) minutes * 60 * 1000);
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putLong("block_until", blockUntil).apply();
    }

    private void clearBlockTime() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().remove("block_until").apply();
    }


    private void sendSuccessNotification(){
        NotificationFarm notificationFarm = new NotificationFarm();
        notificationFarm.showNotification(this, "Завершение!", "У вас завершился процесс кормления животного");
    }


    @Override
    public void showNavigationView() {
        navigationManager.showNavigationView();
    }

    @Override
    public void hideNavigationView() {
        navigationManager.hideNavigationView();
    }
}
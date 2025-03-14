package com.reynem.tamemind;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.reynem.tamemind.utils.NotificationFarm;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;

import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationListener {
    private final Handler handler = new Handler();
    private float progress;
    private TextView shownTime;
    private Button startTimer;

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

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });

        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> showNavigationView());

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        startTimer = findViewById(R.id.startTimer);
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



        startTimer.setOnClickListener(v -> {
            progress = circularSeekBar.getProgress() * 60;
            shownTime = findViewById(R.id.timeLeft);
            startTimer.setVisibility(View.INVISIBLE);
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
        circularSeekBar.setDisablePointer(true);
        if (progress % 60 != 0) progress -= (progress % 60);
        if (progress % 5 != 0) progress -= (progress % 5);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress > 1) {
                    progress--;
                    int minutes = (int) (progress / 60);
                    int seconds = (int) (progress % 60);
                    circularSeekBar.setProgress(minutes);
                    shownTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);
                } else {
                    sendSuccessNotification();
                    circularSeekBar.setDisablePointer(false);
                    startTimer.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

    }

    private void sendSuccessNotification(){
        NotificationFarm notificationFarm = new NotificationFarm();
        notificationFarm.showNotification(this, "Завершение!", "У вас завершился процесс кормления животного");
    }


    @Override
    public void showNavigationView() {
        NavigationView navigationView = findViewById(R.id.navigationMenu);
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", -navigationView.getWidth(), 0);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    @Override
    public void hideNavigationView() {
        NavigationView navigationView = findViewById(R.id.navigationMenu);
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", 0, -navigationView.getWidth());
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
}
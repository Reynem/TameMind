package com.reynem.tamemind.blocker;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.utils.TimerConstants;

import java.util.Locale;

public class BlockedActivity extends AppCompatActivity {

    private TextView timeRemainingText;
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        initViews();
        setupAnimations();
        startTimeUpdater();
    }

    private void initViews() {
        Button backHome = findViewById(R.id.back_to);
        backHome.setOnClickListener(v -> {
            Intent intent = new Intent(BlockedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        timeRemainingText = findViewById(R.id.block_time);
        updateTimeDisplay();
    }

    private void setupAnimations() {
        ImageView lockIcon = findViewById(R.id.lock_icon);
        TextView blockMessage = findViewById(R.id.block_message);
        TextView motivationalText = findViewById(R.id.motivational_text);

        animateViewIn(lockIcon, 0);
        animateViewIn(blockMessage, 200);
        animateViewIn(timeRemainingText, 400);
        animateViewIn(motivationalText, 600);

        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(lockIcon, "rotation", -5f, 5f);
        rotateAnimation.setDuration(2000);
        rotateAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimation.start();
    }

    private void animateViewIn(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void startTimeUpdater() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void updateTimeDisplay() {
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long blockUntil = prefs.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();
        long timeRemaining = blockUntil - currentTime;

        if (timeRemaining > 0) {
            int hours = (int) (timeRemaining / (60 * 60 * 1000));
            int minutes = (int) ((timeRemaining / (60 * 1000)) % 60);
            int seconds = (int) ((timeRemaining / 1000) % 60);

            String time;
            if (hours > 0) {
                time = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
            } else {
                time = String.format(Locale.US, "%d:%02d", minutes, seconds);
            }

            timeRemainingText.setText(time);

            if (timeRemaining < 60000) { // Less than minute - red
                timeRemainingText.setTextColor(0xFFFF8A80);
            } else if (timeRemaining < 300000) { // Less than 5 minutes - yellow
                timeRemainingText.setTextColor(0xFFFFD54F);
            } else {
                timeRemainingText.setTextColor(0xFFB0BEC5);
            }
        } else {
            timeRemainingText.setText(R.string.focus_session_over);
            timeRemainingText.setTextColor(0xFF4CAF50);
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable);
        }
    }
}
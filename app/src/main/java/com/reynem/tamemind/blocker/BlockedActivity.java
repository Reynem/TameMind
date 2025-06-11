package com.reynem.tamemind.blocker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.utils.TimerConstants;

public class BlockedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        Button backHome = findViewById(R.id.back_to);
        backHome.setOnClickListener(v -> {
            Intent intent = new Intent(BlockedActivity.this, MainActivity.class);
            startActivity(intent);
        });

        TextView timeRemainingText = findViewById(R.id.block_time);
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long blockUntil = prefs.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();
        long timeRemaining = blockUntil - currentTime;

        if (timeRemaining > 0) {
            int minutes = (int) (timeRemaining / (60 * 1000));
            int seconds = (int) ((timeRemaining / 1000) % 60);
            String time;
            if (seconds < 10){
                time = minutes + ":0" + seconds;
            } else {
                time = minutes + ":" + seconds;
            }
            timeRemainingText.setText(time);
        } else {
            timeRemainingText.setText(R.string.focus_session_over);
        }

    }
}
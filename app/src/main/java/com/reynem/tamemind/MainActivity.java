package com.reynem.tamemind;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import me.tankery.lib.circularseekbar.CircularSeekBar;


public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private float progress;

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

        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        Button startTimer = findViewById(R.id.startTimer);


        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                if (fromUser) {
                    Log.d("CircularSeekBar", "User Progress: " + progress);
                }
            }
        });

        startTimer.setOnClickListener(v -> {
            progress = circularSeekBar.getProgress();
            startCountdown(circularSeekBar);
        });

    }

    private void startCountdown(CircularSeekBar circularSeekBar) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress > 0) {
                    progress--;
                    circularSeekBar.setProgress(progress);
                    handler.postDelayed(this, 1000);
                }

                else{
                    sendNotification();
                }
            }
        }, 1000);
    }

    private void sendNotification(){
        Log.d("Time stopped", "вы закончили");
    }
}
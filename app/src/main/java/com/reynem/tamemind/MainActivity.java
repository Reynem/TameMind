package com.reynem.tamemind;

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
import com.reynem.tamemind.utils.NotificationFarm;
import android.Manifest;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
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


        CircularSeekBar circularSeekBar = findViewById(R.id.circularSeekBar);
        startTimer = findViewById(R.id.startTimer);
        shownTime = findViewById(R.id.timeLeft);

        circularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                assert circularSeekBar != null;
                progress = circularSeekBar.getProgress();
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

    private void startCountdown(CircularSeekBar circularSeekBar) {
        circularSeekBar.setDisablePointer(true);
        if (progress % 60 != 0) progress -= (progress % 60);

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


}
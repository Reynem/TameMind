package com.reynem.tamemind;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import nl.joery.timerangepicker.TimeRangePicker;

public class MainActivity extends AppCompatActivity {
    Button timeButton;
    int hour, minute;

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

        ConstraintLayout layout = new ConstraintLayout(this);
        layout.setLayoutParams(new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TimeRangePicker timePicker = getTimeRangePicker();

    }

    @NonNull
    private TimeRangePicker getTimeRangePicker() {
        TimeRangePicker timePicker = findViewById(R.id.picker);
        final TimeRangePicker.Time[] timeSelected = {new TimeRangePicker.Time(10)};
        timePicker.setOnTimeChangeListener(new TimeRangePicker.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(@NonNull TimeRangePicker.Time time) {
                Log.d("TimeRangePicker", "Start time: " + time);

                timeSelected[0] = time;
            }

            @Override
            public void onEndTimeChange(@NonNull TimeRangePicker.Time time) {
                Log.d("TimeRangePicker", "End time: " + time);
            }

            @Override
            public void onDurationChange(@NonNull TimeRangePicker.TimeDuration timeDuration) {

            }
        });
        timePicker.setClockVisible(false);
        accessCloser(timeSelected[0]);
        return timePicker;
    }

    private void accessCloser(TimeRangePicker.Time time){
        System.out.println(time); // В разработке
    }
}
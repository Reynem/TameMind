package com.reynem.tamemind.blocker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;

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
    }
}
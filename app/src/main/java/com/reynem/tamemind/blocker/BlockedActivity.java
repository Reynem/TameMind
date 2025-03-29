package com.reynem.tamemind.blocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;

public class BlockedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        TextView message = findViewById(R.id.block_message);
        message.setText("Доступ к этому приложению ограничен");

        Button backHome = findViewById(R.id.back_to);
        backHome.setOnClickListener(v -> {
            Intent intent = new Intent(BlockedActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
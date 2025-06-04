package com.reynem.tamemind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.utils.PinManager;

public class PinActivity extends AppCompatActivity {
    private PinManager pinManager;
    private EditText pinInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_activiity);

        pinManager = new PinManager(this);
        pinInput = findViewById(R.id.pinInput);
        Button confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(v -> {
            String enteredPin = pinInput.getText().toString();
            if (enteredPin.length() < 4) {
                Toast.makeText(this, "PIN должен содержать 4 цифры", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinManager.isPinSet()) {
                if (pinManager.checkPin(enteredPin)) {
                    startMainActivity();
                } else {
                    Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                }
            } else {
                pinManager.savePin(enteredPin);
                Toast.makeText(this, "PIN установлен", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.reynem.tamemind;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.utils.PinManager;

public class PinActivity extends AppCompatActivity {
    private PinManager pinManager;
    private EditText pin1, pin2, pin3, pin4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_activiity);

        pinManager = new PinManager(this);
        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        Button confirmButton = findViewById(R.id.confirmButton);

        setupPinInputListeners();

        confirmButton.setOnClickListener(v -> {
            String enteredPin = getPinFromInputs();
            if (enteredPin.length() < 4) {
                Toast.makeText(this, getString(R.string.pin_advice), Toast.LENGTH_SHORT).show();
                return;
            }

            if (pinManager.isPinSet()) {
                if (pinManager.checkPin(enteredPin)) {
                    startMainActivity();
                } else {
                    Toast.makeText(this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                    clearPinInputs();
                }
            } else {
                pinManager.savePin(enteredPin);
                Toast.makeText(this, getString(R.string.pin_is_established), Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        });
    }

    private void setupPinInputListeners() {
        pin1.addTextChangedListener(new PinTextWatcher(pin1, pin2));
        pin2.addTextChangedListener(new PinTextWatcher(pin2, pin3));
        pin3.addTextChangedListener(new PinTextWatcher(pin3, pin4));
        pin4.addTextChangedListener(new PinTextWatcher(pin4, null));

        pin2.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && pin2.getText().length() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                pin1.requestFocus();
                return true;
            }
            return false;
        });
        pin3.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && pin3.getText().length() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                pin2.requestFocus();
                return true;
            }
            return false;
        });
        pin4.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && pin4.getText().length() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                pin3.requestFocus();
                return true;
            }
            return false;
        });
    }

    private String getPinFromInputs() {
        return pin1.getText().toString() +
                pin2.getText().toString() +
                pin3.getText().toString() +
                pin4.getText().toString();
    }

    private void clearPinInputs() {
        pin1.setText("");
        pin2.setText("");
        pin3.setText("");
        pin4.setText("");
        pin1.requestFocus();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static class PinTextWatcher implements TextWatcher {
        private final EditText currentPinField;
        private final EditText nextPinField;

        public PinTextWatcher(EditText currentPinField, EditText nextPinField) {
            this.currentPinField = currentPinField;
            this.nextPinField = nextPinField;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextPinField != null) {
                nextPinField.requestFocus();
            }
        }
    }
}
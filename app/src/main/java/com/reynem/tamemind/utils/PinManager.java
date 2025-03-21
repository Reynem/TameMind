package com.reynem.tamemind.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PinManager {
    private static final String PREFS_NAME = "secure_prefs";
    private static final String PIN_KEY = "user_pin";
    private SharedPreferences sharedPreferences;

    public PinManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void savePin(String pin) {
        sharedPreferences.edit().putString(PIN_KEY, pin).apply();
    }

    public String getPin() {
        return sharedPreferences.getString(PIN_KEY, null);
    }

    public boolean isPinSet() {
        return getPin() != null;
    }

    public boolean checkPin(String inputPin) {
        return inputPin.equals(getPin());
    }
}

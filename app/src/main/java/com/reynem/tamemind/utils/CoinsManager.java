package com.reynem.tamemind.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class for managing in-game currency (coins).
 * Provides getting, changing and displaying the balance.
 */
public class CoinsManager {

    private final SharedPreferences sharedPreferences;

    /**
     * Coins manager constructor.
     * @param context Application context, necessary for accessing SharedPreferences.
     */
    public CoinsManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(TimerConstants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Gets the current number of coins.
     * @return integer - current coin balance.
     */
    public int getCoins() {
        return sharedPreferences.getInt(TimerConstants.PREF_KEY_COINS, 0);
    }

    /**
     * Saves the new value of the coin balance.
     * @param coins Number of coins to save. Cannot be negative.
     */
    public void saveCoins(int coins) {
        sharedPreferences.edit()
                .putInt(TimerConstants.PREF_KEY_COINS, Math.max(0, coins))
                .apply();
    }

    /**
     * Reduces the number of coins by the specified amount.
     * @param amount Amount to be debited.
     */
    public void removeCoins(int amount) {
        int currentCoins = getCoins();
        saveCoins(currentCoins - amount);
    }

    /**
     * Updates the text field to display the current coin balance.
     * @param coinsDisplay TextView in which to display the number of coins.
     */
    public void updateCoinsDisplay(TextView coinsDisplay) {
        if (coinsDisplay != null) {
            int coins = getCoins();
            coinsDisplay.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(coins));
        }
    }

    /**
     * Calculates the penalty for stopping the timer early.
     * The penalty is 10% of the current balance, but not less than 50 and not more than 1000 coins.
     * @return Calculated number of coins for the penalty.
     */
    public int calculatePenaltyCoins() {
        int currentCoins = getCoins();
        int penalty = Math.min(currentCoins / 10, 1000); // 10% of the balance, but not more than 1000
        return Math.max(penalty, 50); // Minimum penalty 50
    }
}
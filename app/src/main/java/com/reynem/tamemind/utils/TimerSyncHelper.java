package com.reynem.tamemind.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Locale;


public class TimerSyncHelper {
    private static final String TAG = "TimerSyncHelper";
    private final SharedPreferences sharedPreferences;

    public TimerSyncHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(TimerConstants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isTimerActive() {
        long blockUntil = sharedPreferences.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        boolean isActive = blockUntil > System.currentTimeMillis();
        Log.d(TAG, "Timer active check: " + isActive + " (blockUntil: " + blockUntil + ", current: " + System.currentTimeMillis() + ")");
        return isActive;
    }

    public long getRemainingTime() {
        long blockUntil = sharedPreferences.getLong(TimerConstants.PREF_BLOCK_UNTIL, 0);
        long currentTime = System.currentTimeMillis();
        long remaining = Math.max(0, blockUntil - currentTime);
        Log.d(TAG, "Remaining time: " + remaining + "ms");
        return remaining;
    }

    public int getRemainingSeconds() {
        return (int) (getRemainingTime() / 1000);
    }

    public TimerTime getRemainingMinutesAndSeconds() {
        int totalSeconds = getRemainingSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return new TimerTime(minutes, seconds);
    }

    public long getTimerStartTime() {
        return sharedPreferences.getLong(TimerConstants.PREF_TIMER_START_TIME, 0);
    }

    public float getOriginalTimerDuration() {
        return sharedPreferences.getFloat(TimerConstants.PREF_LAST_TIMER_VALUE, 25);
    }

    public boolean wasStoppedEarly() {
        return getRemainingTime() > 5000;
    }

    public void setBlockTime(int minutes) {
        long blockUntil = System.currentTimeMillis() + ((long) minutes * 60 * 1000);
        long currentTime = System.currentTimeMillis();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(TimerConstants.PREF_BLOCK_UNTIL, blockUntil);
        editor.putLong(TimerConstants.PREF_TIMER_START_TIME, currentTime);

        long allTime = sharedPreferences.getLong(TimerConstants.PREF_GET_ALL_TIME, 0L);
        editor.putLong(TimerConstants.PREF_GET_ALL_TIME, allTime + minutes);

        editor.apply();

        Log.d(TAG, "Block time set for " + minutes + " minutes until " + blockUntil);
    }

    public void clearBlockTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TimerConstants.PREF_BLOCK_UNTIL);
        editor.remove(TimerConstants.PREF_TIMER_START_TIME);
        editor.apply();
        Log.d(TAG, "Block time cleared");
    }

    public TimerState getTimerState() {
        boolean isActive = isTimerActive();
        TimerTime remainingTime = getRemainingMinutesAndSeconds();
        long startTime = getTimerStartTime();
        float originalDuration = getOriginalTimerDuration();

        return new TimerState(isActive, remainingTime, startTime, originalDuration);
    }

    public static class TimerTime {
        public final int minutes;
        public final int seconds;

        public TimerTime(int minutes, int seconds) {
            this.minutes = minutes;
            this.seconds = seconds;
        }

        public String getFormattedTime() {
            return String.format(Locale.US, "%d:%02d", minutes, seconds);
        }

        public int getTotalSeconds() {
            return minutes * 60 + seconds;
        }
    }

    public static class TimerState {
        public final boolean isActive;
        public final TimerTime remainingTime;
        public final long startTime;
        public final float originalDuration;

        public TimerState(boolean isActive, TimerTime remainingTime, long startTime, float originalDuration) {
            this.isActive = isActive;
            this.remainingTime = remainingTime;
            this.startTime = startTime;
            this.originalDuration = originalDuration;
        }

        public boolean wasStoppedEarly() {
            return remainingTime.getTotalSeconds() > 5;
        }
    }
}
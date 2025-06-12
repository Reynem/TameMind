package com.reynem.tamemind.history;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.reynem.tamemind.utils.TimerConstants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private final SharedPreferences prefs;
    private final Gson gson;

    public HistoryManager(Context context) {
        this.prefs = context.getSharedPreferences(TimerConstants.PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveCompletedSession(long startTime, long endTime, int durationMinutes) {
        List<TimerSession> history = getSessionHistory();
        TimerSession session = new TimerSession(startTime, endTime, durationMinutes);
        history.add(session);

        // App will save only last 100 sessions
        if (history.size() > 100) {
            history = history.subList(history.size() - 100, history.size());
        }

        String json = gson.toJson(history);
        prefs.edit().putString(TimerConstants.PREF_TIMER_HISTORY, json).apply();
    }

    public List<TimerSession> getSessionHistory() {
        String json = prefs.getString(TimerConstants.PREF_TIMER_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<TimerSession>>(){}.getType();
        List<TimerSession> history = gson.fromJson(json, listType);

        // Sorting(from new to old)
        history.sort((a, b) -> Long.compare(b.getStartTime(), a.getStartTime()));

        return history;
    }

    public int getTotalSessionsCount() {
        return getSessionHistory().size();
    }

    public int getTotalMinutesToday() {
        List<TimerSession> history = getSessionHistory();
        long todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000));

        int totalMinutes = 0;
        for (TimerSession session : history) {
            if (session.getStartTime() >= todayStart) {
                totalMinutes += session.getDurationMinutes();
            }
        }
        return totalMinutes;
    }

    public int getTotalMinutesThisWeek() {
        List<TimerSession> history = getSessionHistory();
        long weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        int totalMinutes = 0;
        for (TimerSession session : history) {
            if (session.getStartTime() >= weekStart) {
                totalMinutes += session.getDurationMinutes();
            }
        }
        return totalMinutes;
    }

    public void clearHistory() {
        prefs.edit().remove(TimerConstants.PREF_TIMER_HISTORY).apply();
    }
}
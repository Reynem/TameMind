package com.reynem.tamemind.history;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimerSession {
    private final long startTime;
    private final long endTime;
    private final int durationMinutes;
    private final String date;
    private final String startTimeFormatted;
    private final String endTimeFormatted;

    public TimerSession(long startTime, long endTime, int durationMinutes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        this.date = dateFormat.format(new Date(startTime));
        this.startTimeFormatted = timeFormat.format(new Date(startTime));
        this.endTimeFormatted = timeFormat.format(new Date(endTime));
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDate() {
        return date;
    }

    public String getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public String getEndTimeFormatted() {
        return endTimeFormatted;
    }

    public String getFormattedDuration() {
        if (durationMinutes >= 60) {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            return hours + "h " + minutes + "m";
        } else {
            return durationMinutes + "m";
        }
    }
}
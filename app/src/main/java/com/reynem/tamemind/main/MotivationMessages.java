package com.reynem.tamemind.main;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.reynem.tamemind.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MotivationMessages {
    private List<String> motivationMessagesList;
    private final Random randomGenerator = new Random();


    public void initializeMotivationMessages(Resources res) {
        motivationMessagesList = new ArrayList<>();
        motivationMessagesList.add(res.getString(R.string.stop_phubbing_));
        motivationMessagesList.add(res.getString(R.string.return_to_your_work_));
        motivationMessagesList.add(res.getString(R.string.do_not_use_your_phone_));
        motivationMessagesList.add(res.getString(R.string.put_your_phone_down_));
        motivationMessagesList.add(res.getString(R.string.focus_on_your_tasks_));
        motivationMessagesList.add(res.getString(R.string.stay_productive_));
        motivationMessagesList.add(res.getString(R.string.break_the_phone_habit_));
        motivationMessagesList.add(res.getString(R.string.time_to_work_not_scroll_));
        motivationMessagesList.add(res.getString(R.string.don_t_let_your_phone_distract_you_));
        motivationMessagesList.add(res.getString(R.string.stay_focused_stay_sharp_));
        motivationMessagesList.add(res.getString(R.string.your_work_needs_you_more_));
        motivationMessagesList.add(res.getString(R.string.eyes_on_your_goals_not_your_screen_));
        motivationMessagesList.add(res.getString(R.string.be_present_not_distracted_));
    }

    public void updateMotivationMessage(View motivationPage) {
        TextView motivationMessageTextView = (TextView) motivationPage;
        if (motivationMessageTextView != null) {
            if (motivationMessagesList != null && !motivationMessagesList.isEmpty()) {
                motivationMessageTextView.setText(motivationMessagesList.get(randomGenerator.nextInt(motivationMessagesList.size())));
            } else {
                Log.w("MainActivity", "Motivation messages list is null or empty.");
            }
        } else {
            Log.e("MainActivity", "TextView motivation not found in layout.");
        }
    }
}

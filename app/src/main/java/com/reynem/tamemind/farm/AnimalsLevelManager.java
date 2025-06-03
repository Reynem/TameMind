package com.reynem.tamemind.farm;

import com.reynem.tamemind.R;

import java.util.Arrays;
import java.util.List;

public class AnimalsLevelManager {
    public AnimalsLevel getCurrentLevel(long totalTime) {
        double progress = (double) totalTime / 40000;
        List<AnimalsLevel> levels = Arrays.asList(
                new AnimalsLevel(1.0, R.string.elephant, R.drawable.elephant),
                new AnimalsLevel(0.8, R.string.cow, R.drawable.cow),
                new AnimalsLevel(0.4, R.string.chicken, R.drawable.chicken),
                new AnimalsLevel(0.0, R.string.mouse, R.drawable.mouse)
        );

        for (AnimalsLevel level : levels) {
            if (progress >= level.threshold) {
                return level;
            }
        }
        return null;
    }

    public int getProgressPercent(long totalTime) {
        return (int) (((double) totalTime / 40000) * 100);
    }
}

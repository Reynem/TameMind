package com.reynem.tamemind.farm;

import android.content.Context;
import android.content.SharedPreferences;
import com.reynem.tamemind.R;
import com.reynem.tamemind.utils.TimerConstants;
import java.util.Arrays;
import java.util.List;

public class AnimalsLevelManager {
    private Context context;
    private SharedPreferences prefs;
    List<AnimalsLevel> levels = Arrays.asList(
            new AnimalsLevel(1.0, R.string.elephant, R.drawable.elephant),
            new AnimalsLevel(0.8, R.string.cow, R.drawable.cow),
            new AnimalsLevel(0.4, R.string.chicken, R.drawable.chicken),
            new AnimalsLevel(0.0, R.string.mouse, R.drawable.mouse)
    );
    List<AnimalsLevel> allAnimals = Arrays.asList(
            new AnimalsLevel(1.0, R.string.elephant, R.drawable.elephant),
            new AnimalsLevel(0.8, R.string.cow, R.drawable.cow),
            new AnimalsLevel(0.4, R.string.chicken, R.drawable.chicken),
            new AnimalsLevel(0.0, R.string.mouse, R.drawable.mouse),
            new AnimalsLevel(0.6, R.string.fox, R.drawable.cutefox),
            new AnimalsLevel(0.2, R.string.hedgehog, R.drawable.cute_hedgehog)
    );
    public AnimalsLevelManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(TimerConstants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public AnimalsLevel getCurrentLevel(long totalTime) {
        double progress = (double) totalTime / 40000;
        String selectedAnimalKey = getSelectedAnimal();

        if (!selectedAnimalKey.isEmpty()) {
            AnimalsLevel selectedLevel = getAnimalByKey(selectedAnimalKey);

            if (selectedLevel != null) {
                boolean unlockedByPurchase = isAnimalUnlocked(selectedAnimalKey);

                boolean unlockedByLevel = false;
                for (AnimalsLevel level : levels) {
                    if (level.nameResId == selectedLevel.nameResId && progress >= level.threshold) {
                        unlockedByLevel = true;
                        break;
                    }
                }

                if (unlockedByPurchase || unlockedByLevel) {
                    return selectedLevel;
                }
            }
        }

        for (AnimalsLevel level : levels) {
            if (progress >= level.threshold) {
                return level;
            }
        }
        return null;
    }

    public int getProgressPercent(long totalTime) {
        return (int) (((double) totalTime / 10000) * 100);
    }

    public List<AnimalsLevel> getLevels() {
        return levels;
    }

    public List<AnimalsLevel> getAllAnimals() {
        return allAnimals;
    }

    public String getSelectedAnimal() {
        return prefs.getString(TimerConstants.PREF_SELECTED_ANIMAL, "");
    }

    public void setSelectedAnimal(String animalKey) {
        prefs.edit().putString(TimerConstants.PREF_SELECTED_ANIMAL, animalKey).apply();
    }

    public boolean isAnimalUnlocked(String animalKey) {
        return prefs.getBoolean(TimerConstants.PREF_KEY_ANIMAL_PREFIX + animalKey, false);
    }

    public AnimalsLevel getAnimalByKey(String animalKey) {
        int nameResId = getNameResIdByKey(animalKey);
        for (AnimalsLevel animal : allAnimals) {
            if (animal.nameResId == nameResId) {
                return animal;
            }
        }
        return null;
    }

    private int getNameResIdByKey(String animalKey) {
        switch (animalKey) {
            case "mouse": return R.string.mouse;
            case "chicken": return R.string.chicken;
            case "cow": return R.string.cow;
            case "elephant": return R.string.elephant;
            case "fox": return R.string.fox;
            case "hedgehog": return R.string.hedgehog;
            default: return R.string.mouse;
        }
    }

    public String getKeyByNameResId(int nameResId) {
        if (nameResId == R.string.mouse) return "mouse";
        if (nameResId == R.string.chicken) return "chicken";
        if (nameResId == R.string.cow) return "cow";
        if (nameResId == R.string.elephant) return "elephant";
        if (nameResId == R.string.fox) return "fox";
        if (nameResId == R.string.hedgehog) return "hedgehog";
        return "mouse";
    }
}
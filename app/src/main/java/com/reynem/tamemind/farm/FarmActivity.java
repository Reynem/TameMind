package com.reynem.tamemind.farm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.reynem.tamemind.history.HistoryActivity;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;
import com.reynem.tamemind.shop.ShopActivity;
import com.reynem.tamemind.utils.CoinsManager;
import com.reynem.tamemind.utils.TimerConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FarmActivity extends AppCompatActivity {
    private TextView coinsDisplay;
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;
    private CoinsManager coinsManager;
    private AnimalsLevelManager manager;
    private FarmAnimalsAdapter animalsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_farm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NavigationView navigationView = findViewById(R.id.navigationMenu);
        navigationManager = new NavigationManager(navigationView); // Assuming NavigationManager constructor takes Context
        drawerLayout = findViewById(R.id.drawerLayout);
        coinsDisplay = findViewById(R.id.coinsAmount);
        coinsManager = new CoinsManager(this);
        manager = new AnimalsLevelManager(this);

        coinsManager.updateCoinsDisplay(coinsDisplay);

        setupNavigation(navigationView);
        setupFarmDisplay();
        setupAnimalSelection();
    }

    private void setupNavigation(NavigationView navigationView) {
        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        View headerView = navigationView.getHeaderView(0);
        ImageView closeButton = headerView.findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_history){
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else if (id == R.id.nav_shop){
                startActivity(new Intent(this, ShopActivity.class));
                return true;
            } else return id == R.id.nav_farm;
        });
    }

    private void setupFarmDisplay() {
        TextView animalName = findViewById(R.id.animalName);
        ImageView animalImage = findViewById(R.id.animalImage);
        LinearProgressIndicator progressAnimal = findViewById(R.id.progressAnimal);
        TextView progressPercent = findViewById(R.id.progressPercent);
//        TextView totalFocusTime = findViewById(R.id.totalFocusTime);
        TextView timeToNext = findViewById(R.id.timeToNext);
        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong(TimerConstants.PREF_GET_ALL_TIME, 0L);

        AnimalsLevel level = manager.getCurrentLevel(totalTime);
        if (level != null) {
            animalName.setText(level.nameResId);
            animalImage.setImageResource(level.imageResId);
            int progress = manager.getProgressPercent(totalTime);
            Log.d("TotalTime", "TotalTime in FarmActivity: " + totalTime);
            Log.d("Progress", "Progress: " + progress);
            progressAnimal.setProgress(progress);
            progressPercent.setText(String.format(Locale.US, "%d%%", progress));

            String formattedTime = formatTime(totalTime);
//            totalFocusTime.setText(formattedTime);

            updateMotivationText(timeToNext, progress);
        }

        MaterialButton changeAnimalButton = findViewById(R.id.changeAnimalButton);
        changeAnimalButton.setOnClickListener(v -> showAnimalSelectionDialog());
    }

    private void setupAnimalSelection() {
        RecyclerView animalsRecyclerView = findViewById(R.id.availableAnimalsRecyclerView);
        if (animalsRecyclerView != null) {
            animalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            List<AnimalsLevel> unlockedAnimals = getUnlockedAnimals();
            animalsAdapter = new FarmAnimalsAdapter(unlockedAnimals, this::selectAnimal);
            animalsRecyclerView.setAdapter(animalsAdapter);
        }
    }

    private List<AnimalsLevel> getUnlockedAnimals() {
        List<AnimalsLevel> unlockedAnimals = new ArrayList<>();
        Set<Integer> addedAnimalIds = new HashSet<>();

        SharedPreferences prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong(TimerConstants.PREF_GET_ALL_TIME, 0L);
        double progress = (double) totalTime / 10000;

        List<AnimalsLevel> allAnimals = manager.getAllAnimals();
        List<AnimalsLevel> levelAnimals = manager.getLevels();

        for (AnimalsLevel animal : allAnimals) {
            String key = manager.getKeyByNameResId(animal.nameResId);

            boolean isUnlockedByPurchase = manager.isAnimalUnlocked(key);

            boolean isUnlockedByLevel = false;
            for (AnimalsLevel level : levelAnimals) {
                if (animal.nameResId == level.nameResId) {
                    if (progress >= level.threshold) {
                        isUnlockedByLevel = true;
                        break;
                    }
                }
            }

            if ((isUnlockedByPurchase || isUnlockedByLevel) && !addedAnimalIds.contains(animal.nameResId)) {
                unlockedAnimals.add(animal);
                addedAnimalIds.add(animal.nameResId);
            }
        }
        return unlockedAnimals;
    }

    private void showAnimalSelectionDialog() {
        List<AnimalsLevel> unlockedAnimals = getUnlockedAnimals();
        if (unlockedAnimals.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_animals_title)
                    .setMessage(R.string.no_animals_message)
                    .setPositiveButton(R.string.go_to_shop, (dialog, which) -> {
                        startActivity(new Intent(this, ShopActivity.class));
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        String[] animalNames = new String[unlockedAnimals.size()];
        for (int i = 0; i < unlockedAnimals.size(); i++) {
            animalNames[i] = getString(unlockedAnimals.get(i).nameResId);
        }

        String currentSelected = manager.getSelectedAnimal();
        int selectedIndex = -1;
        for (int i = 0; i < unlockedAnimals.size(); i++) {
            String key = manager.getKeyByNameResId(unlockedAnimals.get(i).nameResId);
            if (key.equals(currentSelected)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_animal)
                .setSingleChoiceItems(animalNames, selectedIndex, (dialog, which) -> {
                    AnimalsLevel selectedAnimal = unlockedAnimals.get(which);
                    selectAnimal(selectedAnimal);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void selectAnimal(AnimalsLevel animal) {
        String animalKey = manager.getKeyByNameResId(animal.nameResId);
        manager.setSelectedAnimal(animalKey);

        setupFarmDisplay();
    }

    private String formatTime(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    private void updateMotivationText(TextView textView, int progress) {
        String[] motivationalTexts = {
                getString(R.string.every_minute_feeds),
                getString(R.string.keep_going_next_animal),
                getString(R.string.your_dedication_growing),
                getString(R.string.focus_more_evolve),
                getString(R.string.amazing_progress)
        };
        if (progress >= 90) {
            textView.setText(getString(R.string.almost_there_next_level));
        } else if (progress >= 50) {
            textView.setText(getString(R.string.great_progress_halfway));
        } else {
            int randomIndex = (int) (Math.random() * motivationalTexts.length);
            textView.setText(motivationalTexts[randomIndex]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinsManager.updateCoinsDisplay(coinsDisplay);
        setupFarmDisplay();
        setupAnimalSelection();
    }
}
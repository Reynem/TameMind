package com.reynem.tamemind.farm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;

import java.util.Locale;

public class FarmActivity extends AppCompatActivity {

    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;

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
        navigationManager = new NavigationManager(navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);

        setupNavigation(navigationView);
        setupFarmDisplay();
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
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_farm;
        });
    }

    private void setupFarmDisplay() {
        TextView animalName = findViewById(R.id.animalName);
        ImageView animalImage = findViewById(R.id.animalImage);
        LinearProgressIndicator progressAnimal = findViewById(R.id.progressAnimal);
        TextView progressPercent = findViewById(R.id.progressPercent);
        TextView totalFocusTime = findViewById(R.id.totalFocusTime);
        TextView timeToNext = findViewById(R.id.timeToNext);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        long totalTime = prefs.getLong("get_all_time", 0L);

        AnimalsLevelManager manager = new AnimalsLevelManager();
        AnimalsLevel level = manager.getCurrentLevel(totalTime);

        if (level != null) {
            animalName.setText(level.nameResId);
            animalImage.setImageResource(level.imageResId);

            int progress = manager.getProgressPercent(totalTime);
            progressAnimal.setProgress(progress);
            progressPercent.setText(String.format(Locale.US, "%d%%", progress));

            String formattedTime = formatTime(totalTime);
            totalFocusTime.setText(formattedTime);

            updateMotivationText(timeToNext, progress);
        }
    }

    private String formatTime(long timeInMillis) {
        long totalMinutes = timeInMillis / (1000 * 60);
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
}
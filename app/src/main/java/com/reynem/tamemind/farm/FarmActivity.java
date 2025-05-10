package com.reynem.tamemind.farm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;

public class FarmActivity extends AppCompatActivity implements NavigationListener {

    private NavigationManager navigationManager;

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
        navigationManager.hideNavigationView();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home){
                Intent intent = new Intent(FarmActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_settings){
                Intent intent = new Intent(FarmActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_language){
                navigationManager.showLanguageSelectionDialog();
                return true;
            }
            else return id == R.id.nav_farm;
        });
        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> {
            navigationView.setVisibility(NavigationView.VISIBLE);
            showNavigationView();
        });

        TextView animalName = findViewById(R.id.animalName);
        ImageView  animalImage = findViewById(R.id.animalImage);
        ProgressBar progressAnimal = findViewById(R.id.progressAnimal);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        double progress = (double) prefs.getLong("get_all_time", 0L) / 40000;
        if (progress >= 1){
            animalName.setText(R.string.elephant);
            animalImage.setImageResource(R.drawable.elephant);
        } else if (progress > 0.8){
            animalName.setText(R.string.cow);
            animalImage.setImageResource(R.drawable.cow);

        }else if (progress > 0.4){
            animalName.setText(R.string.chicken);
            animalImage.setImageResource(R.drawable.chicken);
        }else {
            animalName.setText(R.string.mouse);
            animalImage.setImageResource(R.drawable.mouse);
        }
        progressAnimal.setMax(100);
        progressAnimal.setProgress((int)progress);

    }



    @Override
    public void showNavigationView() {
        navigationManager.showNavigationView();
    }

    @Override
    public void hideNavigationView() {
        navigationManager.hideNavigationView();
    }
}
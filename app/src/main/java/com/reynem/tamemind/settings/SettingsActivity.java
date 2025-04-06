package com.reynem.tamemind.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;

public class SettingsActivity extends AppCompatActivity implements NavigationListener {
    private NavigationManager navigationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
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
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_farm){

                return true;
            }
            return false;
        });
        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> {
            navigationView.setVisibility(NavigationView.VISIBLE);
            showNavigationView();
        });
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
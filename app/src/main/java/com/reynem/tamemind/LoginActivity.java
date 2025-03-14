package com.reynem.tamemind;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;

public class LoginActivity extends AppCompatActivity implements NavigationListener {
    private NavigationView navigationView;
    private NavigationManager navigationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationView = findViewById(R.id.navigationMenu);
        navigationManager = new NavigationManager(navigationView);
        navigationManager.hideNavigationView();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (id == R.id.nav_farm){

                    return true;
                }
                else if (id == R.id.nav_logout){

                    return true;
                }
                return false;
            }
        });

        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> showNavigationView());

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
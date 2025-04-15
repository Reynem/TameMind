package com.reynem.tamemind.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity implements NavigationListener {
    private EditText editPackageName;
    private TextView textAllowedApps;
    private SharedPreferences prefs;
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
                Intent intent = new Intent(SettingsActivity.this, FarmActivity.class);
                startActivity(intent);
                return true;
            }
            else return id == R.id.nav_settings;
        });
        ImageView closeButton = navigationView.getHeaderView(0).findViewById(R.id.cont);
        closeButton.setOnClickListener(v -> hideNavigationView());

        ImageView openButton = findViewById(R.id.openNav);
        openButton.setOnClickListener(v -> {
            navigationView.setVisibility(NavigationView.VISIBLE);
            showNavigationView();
        });

        editPackageName = findViewById(R.id.edit_package_name);
        Button btnAdd = findViewById(R.id.btn_add);
        Button btnRemove = findViewById(R.id.btn_remove);
        textAllowedApps = findViewById(R.id.text_allowed_apps);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        btnAdd.setOnClickListener(v -> {
            String pkg = editPackageName.getText().toString().trim();
            if (!pkg.isEmpty()) {
                AppBlockerService.addAllowedApp(prefs, pkg);
                updateAllowedAppsText();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String pkg = editPackageName.getText().toString().trim();
            if (!pkg.isEmpty()) {
                AppBlockerService.removeAllowedApp(prefs, pkg);
                updateAllowedAppsText();
            }
        });

        updateAllowedAppsText();


    }

    private void updateAllowedAppsText() {
        Set<String> allowedApps = prefs.getStringSet("allowed_apps", AppBlockerService.getDefaultAllowedApps());
        StringBuilder builder = new StringBuilder("Allowed apps:\n");
        for (String app : allowedApps) {
            builder.append("• ").append(app).append("\n");
        }
        textAllowedApps.setText(builder.toString());
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
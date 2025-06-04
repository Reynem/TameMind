package com.reynem.tamemind.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText editAppName;
    private TextView textAllowedApps;
    private SharedPreferences prefs;
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;

    private final Map<String, AppInfo> appMap = new HashMap<>() {{
        put("youtube", new AppInfo("Youtube", "com.google.android.youtube"));
        put("telegram", new AppInfo("Telegram", "org.telegram.messenger"));
        put("whatsapp", new AppInfo("WhatsApp", "com.whatsapp"));
        put("instagram", new AppInfo("Instagram", "com.instagram.android"));
        put("chrome", new AppInfo("Chrome", "com.android.chrome"));
        put("google", new AppInfo("Google", "com.google.android.googlequicksearchbox"));
        put("facebook", new AppInfo("Facebook", "com.facebook.katana"));
        put("vk", new AppInfo("VK", "com.vkontakte.android"));
    }};

    @SuppressLint("CutPasteId")
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
        drawerLayout = findViewById(R.id.drawerLayout);

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
            } else if (id == R.id.nav_farm) {
                startActivity(new Intent(this, FarmActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_settings;
        });

        editAppName = findViewById(R.id.edit_package_name);
        Button btnAdd = findViewById(R.id.btn_add);
        Button btnRemove = findViewById(R.id.btn_remove);
        textAllowedApps = findViewById(R.id.text_allowed_apps);
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        @SuppressLint("CutPasteId") AutoCompleteTextView autoCompleteTextView = findViewById(R.id.edit_package_name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(appMap.keySet())
        );
        autoCompleteTextView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String input = editAppName.getText().toString().trim().toLowerCase();
            AppInfo app = appMap.get(input);
            if (app != null) {
                AppBlockerService.addAllowedApp(prefs, app.packageName);
                updateAllowedAppsText();
                Toast.makeText(this, app.name + " разрешено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Приложение не найдено в списке", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String input = editAppName.getText().toString().trim().toLowerCase();
            AppInfo app = appMap.get(input);
            if (app != null) {
                AppBlockerService.removeAllowedApp(prefs, app.packageName);
                updateAllowedAppsText();
                Toast.makeText(this, app.name + " запрещено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Приложение не найдено в списке", Toast.LENGTH_SHORT).show();
            }
        });

        updateAllowedAppsText();
    }

    private void updateAllowedAppsText() {
        Set<String> allowedApps = prefs.getStringSet("allowed_apps", AppBlockerService.getDefaultAllowedApps());
        StringBuilder builder = new StringBuilder("Allowed apps:\n");
        for (String pkg : allowedApps) {
            for (AppInfo app : appMap.values()) {
                if (app.packageName.equals(pkg)) {
                    builder.append("• ").append(app.name).append("\n");
                    break;
                }
            }
        }
        textAllowedApps.setText(builder.toString());
    }
}

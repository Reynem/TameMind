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
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.history.HistoryActivity;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.utils.TimerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText editAppName;
    private LinearLayout appsContainer;
    private LinearLayout emptyStateLayout;
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
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_settings;
        });
        editAppName = findViewById(R.id.edit_package_name);
        Button btnAdd = findViewById(R.id.btn_add);
        Button btnRemove = findViewById(R.id.btn_remove);
        appsContainer = findViewById(R.id.appsContainer);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        prefs = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);

        @SuppressLint("CutPasteId")
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.edit_package_name);
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
                Set<String> allowedApps = prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, AppBlockerService.getDefaultAllowedApps());
                if (!allowedApps.contains(app.packageName)) {
                    AppBlockerService.addAllowedApp(prefs, app.packageName);
                    updateAllowedAppsUI();
                    editAppName.setText("");
                    Toast.makeText(this, app.name + " allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, app.name + " is already allowed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Application is not in app list", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String input = editAppName.getText().toString().trim().toLowerCase();
            AppInfo app = appMap.get(input);
            if (app != null) {
                Set<String> allowedApps = prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, AppBlockerService.getDefaultAllowedApps());
                if (allowedApps.contains(app.packageName)) {
                    AppBlockerService.removeAllowedApp(prefs, app.packageName);
                    updateAllowedAppsUI();
                    editAppName.setText("");
                    Toast.makeText(this, app.name + " prohibited", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, app.name + " not found in allowed apps list", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Application is not in app list", Toast.LENGTH_SHORT).show();
            }
        });

        updateAllowedAppsUI();
    }

    private void updateAllowedAppsUI() {
        Set<String> allowedApps = prefs.getStringSet(TimerConstants.PREF_ALLOWED_APPS, AppBlockerService.getDefaultAllowedApps());
        appsContainer.removeAllViews();
        if (allowedApps.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            for (String pkg : allowedApps) {
                AppInfo app = findAppByPackage(pkg);
                if (app != null) {
                    View appItemView = createAppItemView(app);
                    appsContainer.addView(appItemView);
                }
            }
        }
    }

    private AppInfo findAppByPackage(String packageName) {
        for (AppInfo app : appMap.values()) {
            if (app.packageName.equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    private View createAppItemView(AppInfo app) {
        LinearLayout itemLayout = getLinearLayout();

        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(12, 12);
        dotParams.setMargins(0, 8, 16, 0);
        dot.setLayoutParams(dotParams);
        dot.setBackgroundResource(R.drawable.circle_dot);

        TextView appName = new TextView(this);
        appName.setText(app.name);
        appName.setTextSize(16);
        appName.setTextColor(getResources().getColor(android.R.color.black, getTheme()));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        appName.setLayoutParams(textParams);

        itemLayout.addView(dot);
        itemLayout.addView(appName);

        return itemLayout;
    }

    @NonNull
    private LinearLayout getLinearLayout() {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 8);
        itemLayout.setLayoutParams(layoutParams);

        itemLayout.setBackgroundResource(R.drawable.app_item_background);
        return itemLayout;
    }
}
package com.reynem.tamemind.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.MainActivity;
import com.reynem.tamemind.R;
import com.reynem.tamemind.blocker.AppBlockerService;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.navigation.NavigationListener;
import com.reynem.tamemind.navigation.NavigationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements NavigationListener {
    private EditText editAppName;
    private TextView textAllowedApps;
    private SharedPreferences prefs;
    private NavigationManager navigationManager;
    private final Map<String, String> appNameToPackageMap = new HashMap<>() {{
        put("youtube", "com.google.android.youtube");
        put("telegram", "org.telegram.messenger");
        put("whatsapp", "com.whatsapp");
        put("instagram", "com.instagram.android");
        put("chrome", "com.android.chrome");
        put("google", "com.google.android.googlequicksearchbox");
        put("facebook", "com.facebook.katana");
        put("vk", "com.vkontakte.android");
    }};

    private final Map<String, String> appPackageToNameMap = new HashMap<>() {{
        put("com.google.android.youtube", "Youtube");
        put("org.telegram.messenger", "Telegram");
        put("com.whatsapp", "WhatsApp");
        put("com.instagram.android", "Instagram");
        put("com.android.chrome", "Chrome");
        put("com.google.android.googlequicksearchbox", "Google");
        put("com.facebook.katana", "Facebook");
        put("com.vkontakte.android", "VK");
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

        editAppName = findViewById(R.id.edit_package_name);
        Button btnAdd = findViewById(R.id.btn_add);
        Button btnRemove = findViewById(R.id.btn_remove);
        textAllowedApps = findViewById(R.id.text_allowed_apps);
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        @SuppressLint("CutPasteId") AutoCompleteTextView autoCompleteTextView = findViewById(R.id.edit_package_name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(appNameToPackageMap.keySet())
        );
        autoCompleteTextView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String input = editAppName.getText().toString().trim().toLowerCase();
            String pkg = appNameToPackageMap.get(input);
            if (pkg != null) {
                AppBlockerService.addAllowedApp(prefs, pkg);
                updateAllowedAppsText();
                Toast.makeText(this, input + " разрешено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Приложение не найдено в списке", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String input = editAppName.getText().toString().trim().toLowerCase();
            String pkg = appNameToPackageMap.get(input);
            if (pkg != null) {
                AppBlockerService.removeAllowedApp(prefs, pkg);
                updateAllowedAppsText();
                Toast.makeText(this, input + " запрещено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Приложение не найдено в списке", Toast.LENGTH_SHORT).show();
            }
        });

        updateAllowedAppsText();

    }

    private void updateAllowedAppsText() {
        Set<String> allowedApps = prefs.getStringSet("allowed_apps", AppBlockerService.getDefaultAllowedApps());
        StringBuilder builder = new StringBuilder("Allowed apps:\n");
        for (String app : allowedApps) {
            if (appPackageToNameMap.get(app) == null) continue;
            builder.append("• ").append(appPackageToNameMap.get(app)).append("\n");
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
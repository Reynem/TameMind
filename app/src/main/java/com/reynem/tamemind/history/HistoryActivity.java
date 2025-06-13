package com.reynem.tamemind.history;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;
    private HistoryManager historyManager;
    private SessionHistoryAdapter adapter;
    private TextView totalSessionsText;
    private TextView todayMinutesText;
    private TextView weekMinutesText;
    private TextView emptyStateText;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        historyManager = new HistoryManager(this);

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        navigationManager = new NavigationManager(navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);

        setupNavigation(navigationView);
        setupViews(); // Сначала настраиваем View
        loadHistoryData(); // Затем загружаем в них данные
    }

    private void setupNavigation(NavigationView navigationView) {
        // ... ваш код без изменений ...
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
            } else if (id == R.id.nav_farm) {
                startActivity(new Intent(this, FarmActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_history;
        });
    }

    private void setupViews() {
        totalSessionsText = findViewById(R.id.totalSessions);
        todayMinutesText = findViewById(R.id.todayMinutes);
        weekMinutesText = findViewById(R.id.weekMinutes);
        emptyStateText = findViewById(R.id.emptyStateText);
        recyclerView = findViewById(R.id.historyRecyclerView);

        // --- ИЗМЕНЕНИЕ 1 ---
        // Настраиваем RecyclerView и создаем адаптер ОДИН РАЗ здесь.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionHistoryAdapter(); // Используем новый конструктор
        recyclerView.setAdapter(adapter); // Устанавливаем адаптер сразу

        // Кнопка очистки истории
        findViewById(R.id.clearHistoryButton).setOnClickListener(v -> showClearHistoryDialog());
    }

    private void loadHistoryData() {
        List<TimerSession> sessions = historyManager.getSessionHistory();

        // Обновляем статистику
        totalSessionsText.setText(getString(R.string.total_sessions, historyManager.getTotalSessionsCount()));
        todayMinutesText.setText(formatMinutes(historyManager.getTotalMinutesToday()));
        weekMinutesText.setText(formatMinutes(historyManager.getTotalMinutesThisWeek()));


        // --- ИЗМЕНЕНИЕ 2 ---
        // Логика упростилась. Просто обновляем данные в адаптере.
        // DiffUtil сам разберется, что делать (добавить, удалить или обновить элементы).
        adapter.updateSessions(sessions);


        // Показываем список или пустое состояние (эта логика остается)
        if (sessions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private String formatMinutes(int minutes) {
        // ... ваш код без изменений ...
        if (minutes >= 60) {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        } else {
            return minutes + "m";
        }
    }

    private void showClearHistoryDialog() {
        // ... ваш код без изменений ...
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_history)
                .setMessage(R.string.clear_history_confirmation)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    historyManager.clearHistory();
                    loadHistoryData(); // Перезагружаем данные после очистки
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume правильно вызывает loadHistoryData, чтобы обновлять
        // список при возвращении на экран. Это остается без изменений.
        loadHistoryData();
    }
}
package com.reynem.tamemind.shop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;
import com.reynem.tamemind.farm.FarmActivity;
import com.reynem.tamemind.history.HistoryActivity;
import com.reynem.tamemind.main.MainActivity;
import com.reynem.tamemind.navigation.NavigationManager;
import com.reynem.tamemind.settings.SettingsActivity;
import com.reynem.tamemind.utils.CoinsManager;
import com.reynem.tamemind.utils.TimerConstants;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ShopActivity extends AppCompatActivity {
    private NavigationManager navigationManager;
    private DrawerLayout drawerLayout;
    private ShopAnimalsAdapter animalsAdapter;
    private SharedPreferences sharedPreferences;
    private TextView coinsDisplay;
    private List<ShopAnimal> animals;
    private int currentCoins;
    private CoinsManager coinsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(TimerConstants.PREFS_NAME, MODE_PRIVATE);

        NavigationView navigationView = findViewById(R.id.navigationMenu);
        navigationManager = new NavigationManager(navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        coinsDisplay = findViewById(R.id.coinsAmount);

        setupNavigation(navigationView);
        setupShop();
        coinsManager.updateCoinsDisplay(coinsDisplay);
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
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_farm) {
                startActivity(new Intent(this, FarmActivity.class));
                return true;
            } else if (id == R.id.nav_language) {
                navigationManager.showLanguageSelectionDialog();
                return true;
            } else return id == R.id.nav_shop;
        });
    }

    private void setupShop() {
        RecyclerView animalsRecyclerView = findViewById(R.id.animalsRecyclerView);
        animalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Temporarily set the minimum price
        animals = Arrays.asList(
                new ShopAnimal(R.string.fox, R.drawable.cutefox, 1, isAnimalUnlocked("fox")),
                new ShopAnimal(R.string.hedgehog, R.drawable.cute_hedgehog, 2, isAnimalUnlocked("hedgehog"))
        );

        if (!isAnimalUnlocked("mouse")) {
            unlockAnimal("mouse");
            animals.get(0).setUnlocked(true);
        }

        animalsAdapter = new ShopAnimalsAdapter(animals, this::onAnimalClick);
        animalsRecyclerView.setAdapter(animalsAdapter);
    }

    private void onAnimalClick(ShopAnimal animal) {
        if (animal.isUnlocked()) {
            Toast.makeText(this, R.string.already_owned, Toast.LENGTH_SHORT).show();
            return;
        }

        currentCoins = coinsManager.getCoins();

        if (currentCoins >= animal.getPrice()) {
            showPurchaseConfirmationDialog(animal);
        } else {
            showInsufficientFundsDialog(animal);
        }
    }

    private void showPurchaseConfirmationDialog(ShopAnimal animal) {
        String animalName = getString(animal.getNameResId());
        String message = getString(R.string.purchase_confirmation, animalName,
                NumberFormat.getNumberInstance(Locale.getDefault()).format(animal.getPrice()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_purchase)
                .setMessage(message)
                .setPositiveButton(R.string.purchase, (dialog, which) -> purchaseAnimal(animal))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showInsufficientFundsDialog(ShopAnimal animal) {
        int needed = animal.getPrice() - currentCoins;
        String message = getString(R.string.insufficient_funds,
                NumberFormat.getNumberInstance(Locale.getDefault()).format(needed));

        new AlertDialog.Builder(this)
                .setTitle(R.string.insufficient_funds_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void purchaseAnimal(ShopAnimal animal) {
        int newCoinsAmount = currentCoins - animal.getPrice();
        coinsManager.saveCoins(newCoinsAmount);

        String animalKey = getAnimalKeyFromResourceId(animal.getNameResId());
        unlockAnimal(animalKey);
        animal.setUnlocked(true);

        int position = animals.indexOf(animal);
        if (position != -1) {
            animalsAdapter.notifyItemChanged(position);
        } else {
            animalsAdapter.notifyDataSetChanged();
        }

        coinsManager.updateCoinsDisplay(coinsDisplay);

        String animalName = getString(animal.getNameResId());
        Toast.makeText(this, getString(R.string.purchase_successful, animalName),
                Toast.LENGTH_SHORT).show();
    }

    private String getAnimalKeyFromResourceId(int nameResId) {
        if (nameResId == R.string.mouse) return "mouse";
        if (nameResId == R.string.chicken) return "chicken";
        if (nameResId == R.string.cow) return "cow";
        if (nameResId == R.string.elephant) return "elephant";
        if (nameResId == R.string.fox) return "fox";
        if (nameResId == R.string.hedgehog) return "hedgehog";
        return "unknown";
    }

    private boolean isAnimalUnlocked(String animalKey) {
        return sharedPreferences.getBoolean(TimerConstants.PREF_KEY_ANIMAL_PREFIX + animalKey, false);
    }

    private void unlockAnimal(String animalKey) {
        sharedPreferences.edit()
                .putBoolean(TimerConstants.PREF_KEY_ANIMAL_PREFIX + animalKey, true)
                .apply();
    }

    public void addCoins(int amount) {
        int currentCoins = coinsManager.getCoins();
        coinsManager.saveCoins(currentCoins + amount);
        coinsManager.updateCoinsDisplay(coinsDisplay);
    }

    public String[] getUnlockedAnimals() {
        String[] animalKeys = {"mouse", "chicken", "cow", "elephant", "fox"};
        return Arrays.stream(animalKeys)
                .filter(this::isAnimalUnlocked)
                .toArray(String[]::new);
    }

    @Override
    protected void onResume() {
        super.onResume();
        coinsManager.updateCoinsDisplay(coinsDisplay);
    }
}
package com.reynem.tamemind.navigation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;

public class NavigationManager{
    private final NavigationView navigationView;

    public NavigationManager(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    public void showLanguageSelectionDialog() {
        final String[] displayLanguages = {"English", "Русский"};
        final String[] languageCodes = {"en", "ru"};

        AlertDialog.Builder builder = new AlertDialog.Builder(navigationView.getContext());
        builder.setTitle(R.string.choose_language);
        builder.setItems(displayLanguages, (dialog, which) -> {
            String selectedLanguageCode = languageCodes[which];
            changeAppLanguage(selectedLanguageCode);
        });
        builder.show();
    }

    private void changeAppLanguage(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

}

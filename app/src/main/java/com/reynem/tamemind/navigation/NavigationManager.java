package com.reynem.tamemind.navigation;

import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.navigation.NavigationView;
import com.reynem.tamemind.R;

public class NavigationManager implements NavigationListener{
    private final NavigationView navigationView;

    public NavigationManager(NavigationView navigationView) {
        this.navigationView = navigationView;
        hideNavigationView();
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


    @Override
    public void showNavigationView() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", -navigationView.getWidth(), 0);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    @Override
    public void hideNavigationView() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", 0, -navigationView.getWidth());
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

}

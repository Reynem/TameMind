package com.reynem.tamemind.navigation;

import android.animation.ObjectAnimator;

import com.google.android.material.navigation.NavigationView;

public class NavigationManager implements NavigationListener{
    private final NavigationView navigationView;

    public NavigationManager(NavigationView navigationView) {
        this.navigationView = navigationView;
    }

    @Override
    public void showNavigationView() {

    }

    @Override
    public void hideNavigationView() {

    }

    private void animateNavigationView(float from, float to){
        ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", from, to);

    }
}

package com.reynem.tamemind.navigation;

import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.navigation.NavigationView;
public class NavigationManager implements NavigationListener{
    private final NavigationView navigationView;

    public NavigationManager(NavigationView navigationView) {
        this.navigationView = navigationView;
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

package com.reynem.tamemind.farm;

public class AnimalsLevel {
    double threshold;
    int nameResId;
    int imageResId;

    public AnimalsLevel(double threshold, int nameResId, int imageResId) {
        this.threshold = threshold;
        this.nameResId = nameResId;
        this.imageResId = imageResId;
    }
}

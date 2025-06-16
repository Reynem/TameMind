package com.reynem.tamemind.shop;

public class ShopAnimal {
    private final int nameResId;
    private int imageResId;
    private int price;
    private boolean isUnlocked;

    public ShopAnimal(int nameResId, int imageResId, int price, boolean isUnlocked) {
        this.nameResId = nameResId;
        this.imageResId = imageResId;
        this.price = price;
        this.isUnlocked = isUnlocked;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getPrice() {
        return price;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }
}
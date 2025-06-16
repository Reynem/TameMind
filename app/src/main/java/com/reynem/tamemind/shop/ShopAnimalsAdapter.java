package com.reynem.tamemind.shop;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.reynem.tamemind.R;

import java.util.List;

public class ShopAnimalsAdapter extends RecyclerView.Adapter<ShopAnimalsAdapter.AnimalViewHolder> {

    private final List<ShopAnimal> animals;
    private final OnAnimalClickListener listener;

    public interface OnAnimalClickListener {
        void onAnimalClick(ShopAnimal animal);
    }

    public ShopAnimalsAdapter(List<ShopAnimal> animals, OnAnimalClickListener listener) {
        this.animals = animals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        ShopAnimal animal = animals.get(position);
        holder.bind(animal, listener);
    }

    @Override
    public int getItemCount() {
        return animals.size();
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView animalCard;
        private final ImageView animalImage;
        private final TextView animalName;
        private final TextView animalPrice;
        private final MaterialButton purchaseButton;
        private final View lockOverlay;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            animalCard = itemView.findViewById(R.id.animalCard);
            animalImage = itemView.findViewById(R.id.animalImage);
            animalName = itemView.findViewById(R.id.animalName);
            animalPrice = itemView.findViewById(R.id.animalPrice);
            purchaseButton = itemView.findViewById(R.id.purchaseButton);
            lockOverlay = itemView.findViewById(R.id.lockOverlay);
        }

        @SuppressLint("SetTextI18n")
        public void bind(ShopAnimal animal, OnAnimalClickListener listener) {
            animalName.setText(animal.getNameResId());
            animalImage.setImageResource(animal.getImageResId());

            if (animal.isUnlocked()) {
                lockOverlay.setVisibility(View.GONE);
                purchaseButton.setText(R.string.owned);
                purchaseButton.setEnabled(false);
                animalPrice.setVisibility(View.GONE);
            } else {
                lockOverlay.setVisibility(View.VISIBLE);
                purchaseButton.setText(R.string.purchase);
                purchaseButton.setEnabled(true);
                animalPrice.setText(animal.getPrice() + " $");
                animalPrice.setVisibility(View.VISIBLE);
            }

            animalCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAnimalClick(animal);
                }
            });

            purchaseButton.setOnClickListener(v -> {
                if (listener != null && !animal.isUnlocked()) {
                    listener.onAnimalClick(animal);
                }
            });
        }
    }
}
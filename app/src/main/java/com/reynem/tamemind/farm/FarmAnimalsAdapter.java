package com.reynem.tamemind.farm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.reynem.tamemind.R;

import java.util.List;

public class FarmAnimalsAdapter extends RecyclerView.Adapter<FarmAnimalsAdapter.AnimalViewHolder> {

    private final List<AnimalsLevel> animals;
    private final OnAnimalSelectListener listener;
    private int selectedPosition = -1;

    public interface OnAnimalSelectListener {
        void onAnimalSelect(AnimalsLevel animal);
    }

    public FarmAnimalsAdapter(List<AnimalsLevel> animals, OnAnimalSelectListener listener) {
        this.animals = animals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farm_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        AnimalsLevel animal = animals.get(position);
        holder.bind(animal, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int previousSelected = selectedPosition;
            selectedPosition = currentPosition;

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onAnimalSelect(animals.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return animals.size();
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= animals.size()) {
            return;
        }

        int previousSelected = selectedPosition;
        selectedPosition = position;

        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView animalCard;
        private final ImageView animalImage;
        private final TextView animalName;
        private final View selectedIndicator;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            animalCard = itemView.findViewById(R.id.animalCard);
            animalImage = itemView.findViewById(R.id.animalImage);
            animalName = itemView.findViewById(R.id.animalName);
            selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
        }

        public void bind(AnimalsLevel animal, boolean isSelected) {
            animalName.setText(animal.nameResId);
            animalImage.setImageResource(animal.imageResId);

            selectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            if (isSelected) {
                animalCard.setStrokeWidth(4);
                animalCard.setStrokeColor(itemView.getContext().getColor(R.color.primary_color));
            } else {
                animalCard.setStrokeWidth(0);
            }
        }
    }
}
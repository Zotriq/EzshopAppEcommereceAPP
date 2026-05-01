package com.example.ezshopapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private List<String> colorHexList;
    private int selectedPosition = 0;
    private OnColorClickListener listener;

    public interface OnColorClickListener {
        void onColorClick(String colorHex);
    }

    public ColorAdapter(List<String> colorHexList, OnColorClickListener listener) {
        this.colorHexList = colorHexList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String hexColor = colorHexList.get(position);
        
        GradientDrawable background = (GradientDrawable) holder.colorCircle.getBackground();
        
        // Removed try-catch as requested. 
        // Note: If hexColor is invalid (e.g. doesn't start with '#'), the app will crash.
        if (hexColor != null && !hexColor.isEmpty()) {
            background.setColor(Color.parseColor(hexColor));
        }

        // Show/Hide selection ring
        if (selectedPosition == position) {
            holder.selectedRing.setVisibility(View.VISIBLE);
        } else {
            holder.selectedRing.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onColorClick(hexColor);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return colorHexList != null ? colorHexList.size() : 0;
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorCircle, selectedRing;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorCircle = itemView.findViewById(R.id.colorCircle);
            selectedRing = itemView.findViewById(R.id.selectedRing);
        }
    }
}

package com.example.ezshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrendingSearchAdapter extends RecyclerView.Adapter<TrendingSearchAdapter.ViewHolder> {

    private List<String> trendingList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String query);
    }

    public TrendingSearchAdapter(List<String> trendingList, OnItemClickListener listener) {
        this.trendingList = trendingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = trendingList.get(position);
        holder.tvTagName.setText(query);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(query);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trendingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagName = itemView.findViewById(R.id.tvTagName);
        }
    }
}

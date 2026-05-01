package com.example.ezshopapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> banners;

    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promo_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        holder.title.setText(banner.getTitle());
        holder.subtitle.setText(banner.getSubtitle());
        holder.discount.setText(banner.getDiscountText());
        Glide.with(holder.itemView.getContext()).load(banner.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle, discount;
        ImageView image;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.promoTitle);
            subtitle = itemView.findViewById(R.id.promoSubtitle);
            discount = itemView.findViewById(R.id.promoDiscount);
            image = itemView.findViewById(R.id.promoImage);
        }
    }
}

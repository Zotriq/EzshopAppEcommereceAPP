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

public class MainHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 0;
    private static final int BEST_SELLERS = 1;
    private static final int TITLE = 2;
    private static final int RECOMMENDATION = 3;

    private List<Product> bestSellers;
    private List<Product> recommendations;

    public MainHomeAdapter(List<Product> bestSellers, List<Product> recommendations) {
        this.bestSellers = bestSellers;
        this.recommendations = recommendations;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return HEADER;
        if (position == 1) return BEST_SELLERS;
        if (position == 2) return TITLE;
        return RECOMMENDATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_home_header, parent, false));
        } else if (viewType == BEST_SELLERS) {
            return new BestSellersViewHolder(inflater.inflate(R.layout.item_best_sellers_list, parent, false));
        } else if (viewType == TITLE) {
            return new TitleViewHolder(inflater.inflate(R.layout.item_section_title, parent, false));
        } else {
            return new RecommendationViewHolder(inflater.inflate(R.layout.item_recommendation, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BestSellersViewHolder) {
            ((BestSellersViewHolder) holder).bind(bestSellers);
        } else if (holder instanceof RecommendationViewHolder) {
            // Subtracting 3 because of Header (0), BestSellers (1), and Title (2)
            Product product = recommendations.get(position - 3);
            ((RecommendationViewHolder) holder).bind(product);
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).titleTv.setText("Recommendation");
        }
    }

    @Override
    public int getItemCount() {
        return 3 + recommendations.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) { super(itemView); }
    }

    static class BestSellersViewHolder extends RecyclerView.ViewHolder {
        RecyclerView innerRecycler;
        public BestSellersViewHolder(@NonNull View itemView) {
            super(itemView);
            innerRecycler = itemView.findViewById(R.id.innerRecyclerView);
        }
        void bind(List<Product> products) {
            ProductAdapter adapter = new ProductAdapter(products);
            innerRecycler.setAdapter(adapter);
        }
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv;
        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.sectionTitle);
        }
    }

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, rating, soldCount, location;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            rating = itemView.findViewById(R.id.productRating);
            soldCount = itemView.findViewById(R.id.soldCount);
            location = itemView.findViewById(R.id.productLocation);
        }

        void bind(Product product) {
            name.setText(product.getName());
            price.setText("$ " + product.getPrice());
            rating.setText(String.valueOf(product.getRating()));
            soldCount.setText(product.getSoldCount());
            location.setText(product.getLocation());
            Glide.with(itemView.getContext()).load(product.getImageUrl()).into(image);
        }
    }
}

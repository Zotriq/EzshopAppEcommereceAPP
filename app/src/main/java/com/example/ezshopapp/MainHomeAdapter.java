package com.example.ezshopapp;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BEST_SELLERS = 1;
    private static final int TYPE_RECOMMENDATION_TITLE = 2;
    private static final int TYPE_RECOMMENDATION = 3;

    private List<Product> bestSellers;
    private List<Product> recommendations;
    private OnSearchListener searchListener;

    public interface OnSearchListener {
        void onSearch(String query);
    }

    public MainHomeAdapter(List<Product> bestSellers, List<Product> recommendations, OnSearchListener searchListener) {
        this.bestSellers = bestSellers;
        this.recommendations = recommendations;
        this.searchListener = searchListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) return TYPE_BEST_SELLERS;
        if (position == 2) return TYPE_RECOMMENDATION_TITLE;
        return TYPE_RECOMMENDATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_home_header, parent, false), searchListener);
        } else if (viewType == TYPE_BEST_SELLERS) {
            return new BestSellersViewHolder(inflater.inflate(R.layout.item_best_sellers_list, parent, false));
        } else if (viewType == TYPE_RECOMMENDATION_TITLE) {
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
            int recPosition = position - 3;
            if (recPosition >= 0 && recPosition < recommendations.size()) {
                ((RecommendationViewHolder) holder).bind(recommendations.get(recPosition));
            }
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return 3 + recommendations.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;
        ImageView clearSearch;

        public HeaderViewHolder(@NonNull View itemView, OnSearchListener listener) {
            super(itemView);
            searchEditText = itemView.findViewById(R.id.searchEditText);
            clearSearch = itemView.findViewById(R.id.clearSearch);
            
            // Trigger search when the user presses the "Search" button on the keyboard
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (listener != null) {
                        listener.onSearch(searchEditText.getText().toString());
                    }
                    return true;
                }
                return false;
            });

            // When the "X" button is clicked, clear the text and show all products again
            clearSearch.setOnClickListener(v -> {
                searchEditText.setText("");
                if (listener != null) {
                    listener.onSearch("");
                }
            });
        }
    }

    static class BestSellersViewHolder extends RecyclerView.ViewHolder {
        RecyclerView innerRecycler;
        TextView seeAll;

        public BestSellersViewHolder(@NonNull View itemView) {
            super(itemView);
            innerRecycler = itemView.findViewById(R.id.innerRecyclerView);
            seeAll = itemView.findViewById(R.id.seeAllBestSellers);
        }

        void bind(List<Product> products) {
            ProductAdapter adapter = new ProductAdapter(products);
            innerRecycler.setAdapter(adapter);
            
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), SeeAllActivity.class);
                intent.putExtra("category", "best_seller");
                intent.putExtra("title", "Best Sellers");
                itemView.getContext().startActivity(intent);
            });
        }
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView title, seeAll;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
            seeAll = itemView.findViewById(R.id.seeAllRecommended);
        }

        void bind() {
            title.setText("Recommendation");
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), SeeAllActivity.class);
                intent.putExtra("category", "recommended");
                intent.putExtra("title", "Recommended");
                itemView.getContext().startActivity(intent);
            });
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

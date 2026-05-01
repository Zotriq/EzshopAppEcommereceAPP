package com.example.ezshopapp;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BANNER_LIST = 1;
    private static final int TYPE_BEST_SELLERS = 2;
    private static final int TYPE_RECOMMENDATION_TITLE = 3;
    private static final int TYPE_RECOMMENDATION = 4;

    private List<Product> bestSellers;
    private List<Product> recommendations;
    private List<String> categories;
    private List<Banner> banners;
    private OnSearchListener searchListener;
    private OnCategoryClickListener categoryClickListener;

    public interface OnSearchListener {
        void onSearch(String query);
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public MainHomeAdapter(List<Product> bestSellers, List<Product> recommendations, List<String> categories,
                           List<Banner> banners, OnSearchListener searchListener, OnCategoryClickListener categoryClickListener) {
        this.bestSellers = bestSellers;
        this.recommendations = recommendations;
        this.categories = categories;
        this.banners = banners;
        this.searchListener = searchListener;
        this.categoryClickListener = categoryClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) return TYPE_BANNER_LIST;
        if (position == 2) return TYPE_BEST_SELLERS;
        if (position == 3) return TYPE_RECOMMENDATION_TITLE;
        return TYPE_RECOMMENDATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_home_header, parent, false),
                    categories, searchListener, categoryClickListener);
        } else if (viewType == TYPE_BANNER_LIST) {
            return new BannersListViewHolder(inflater.inflate(R.layout.item_banners_list, parent, false));
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
        if (holder instanceof BannersListViewHolder) {
            ((BannersListViewHolder) holder).bind(banners);
        } else if (holder instanceof BestSellersViewHolder) {
            ((BestSellersViewHolder) holder).bind(bestSellers);
        } else if (holder instanceof RecommendationViewHolder) {
            int recPosition = position - 4;
            if (recPosition >= 0 && recPosition < recommendations.size()) {
                ((RecommendationViewHolder) holder).bind(recommendations.get(recPosition));
            }
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return 4 + recommendations.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;
        ImageView clearSearch;
        RecyclerView categoryRecyclerView;

        public HeaderViewHolder(@NonNull View itemView, List<String> categories,
                                OnSearchListener searchListener, OnCategoryClickListener categoryListener) {
            super(itemView);
            searchEditText = itemView.findViewById(R.id.searchEditText);
            clearSearch = itemView.findViewById(R.id.clearSearch);
            categoryRecyclerView = itemView.findViewById(R.id.categoryRecyclerView);

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchListener != null) {
                        searchListener.onSearch(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            if (clearSearch != null) {
                clearSearch.setOnClickListener(v -> {
                    searchEditText.setText("");
                    if (searchListener != null) {
                        searchListener.onSearch("");
                    }
                });
            }

            if (categoryRecyclerView != null) {
                categoryRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                CategoryAdapter adapter = new CategoryAdapter(categories, categoryListener::onCategoryClick);
                categoryRecyclerView.setAdapter(adapter);
            }
        }
    }

    static class BannersListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView bannerRecyclerView;

        public BannersListViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerRecyclerView = itemView.findViewById(R.id.bannerRecyclerView);
        }

        void bind(List<Banner> banners) {
            if (bannerRecyclerView != null) {
                bannerRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                BannerAdapter adapter = new BannerAdapter(banners);
                bannerRecyclerView.setAdapter(adapter);
            }
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

            if (seeAll != null) {
                seeAll.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), SeeAllActivity.class);
                    intent.putExtra("category", "best_seller");
                    intent.putExtra("title", "Best Sellers");
                    itemView.getContext().startActivity(intent);
                });
            }
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
            if (title != null) title.setText("Recommendation");
            if (seeAll != null) {
                seeAll.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), SeeAllActivity.class);
                    intent.putExtra("category", "recommended");
                    intent.putExtra("title", "Recommended");
                    itemView.getContext().startActivity(intent);
                });
            }
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

            // Navigate to ProductDetailActivity on click
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ProductDetailActivity.class);
                intent.putExtra("product", product);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}

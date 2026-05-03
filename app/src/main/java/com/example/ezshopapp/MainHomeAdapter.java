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

    private final List<Product> bestSellers;
    private final List<Product> recommendations;
    private final List<Category> categories;
    private final List<Banner> banners;
    private final OnSearchListener searchListener;
    private final OnCategoryClickListener categoryClickListener;
    private boolean hasUnreadNotifications = false;

    public interface OnSearchListener {
        void onSearch(String query);
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public MainHomeAdapter(List<Product> bestSellers, List<Product> recommendations, 
                           List<Category> categories, List<Banner> banners, 
                           OnSearchListener searchListener, OnCategoryClickListener categoryClickListener) {
        this.bestSellers = bestSellers;
        this.recommendations = recommendations;
        this.categories = categories;
        this.banners = banners;
        this.searchListener = searchListener;
        this.categoryClickListener = categoryClickListener;
    }

    public void setHasUnreadNotifications(boolean hasUnread) {
        this.hasUnreadNotifications = hasUnread;
        notifyItemChanged(0);
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
                    searchListener, categoryClickListener);
        } else if (viewType == TYPE_BANNER_LIST) {
            return new BannersListViewHolder(inflater.inflate(R.layout.item_banners_list, parent, false));
        } else if (viewType == TYPE_BEST_SELLERS) {
            return new BestSellersViewHolder(inflater.inflate(R.layout.item_best_sellers_list, parent, false));
        } else if (viewType == TYPE_RECOMMENDATION_TITLE) {
            return new TitleViewHolder(inflater.inflate(R.layout.item_section_title, parent, false), "Recommendation", true);
        } else {
            return new RecommendationViewHolder(inflater.inflate(R.layout.item_recommendation, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(categories, hasUnreadNotifications);
        } else if (holder instanceof BannersListViewHolder) {
            ((BannersListViewHolder) holder).bind(banners);
        } else if (holder instanceof BestSellersViewHolder) {
            ((BestSellersViewHolder) holder).bind(bestSellers);
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).bind();
        } else if (holder instanceof RecommendationViewHolder) {
            int recPosition = position - 4;
            if (recPosition >= 0 && recommendations != null && recPosition < recommendations.size()) {
                ((RecommendationViewHolder) holder).bind(recommendations.get(recPosition));
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 4;
        if (recommendations != null) {
            count += recommendations.size();
        }
        return count;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;
        ImageView clearSearch, btnNotification;
        View notificationBadge;
        RecyclerView categoryRecyclerView;
        OnSearchListener searchListener;
        OnCategoryClickListener categoryListener;

        public HeaderViewHolder(@NonNull View itemView, OnSearchListener searchListener, OnCategoryClickListener categoryListener) {
            super(itemView);
            this.searchListener = searchListener;
            this.categoryListener = categoryListener;
            searchEditText = itemView.findViewById(R.id.searchEditText);
            clearSearch = itemView.findViewById(R.id.clearSearch);
            btnNotification = itemView.findViewById(R.id.btnNotification);
            notificationBadge = itemView.findViewById(R.id.notificationBadge);
            categoryRecyclerView = itemView.findViewById(R.id.categoryRecyclerView);

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (HeaderViewHolder.this.searchListener != null) {
                        HeaderViewHolder.this.searchListener.onSearch(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            if (clearSearch != null) {
                clearSearch.setOnClickListener(v -> {
                    searchEditText.setText("");
                });
            }

            if (btnNotification != null) {
                btnNotification.setOnClickListener(v -> {
                    itemView.getContext().startActivity(new Intent(itemView.getContext(), NotificationsActivity.class));
                });
            }
            
            if (categoryRecyclerView != null) {
                categoryRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
        }

        void bind(List<Category> categories, boolean hasUnread) {
            if (notificationBadge != null) {
                notificationBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }
            if (categoryRecyclerView != null && categories != null) {
                CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                    if (categoryListener != null && category != null) {
                        categoryListener.onCategoryClick(category.getName());
                    }
                });
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
            if (bannerRecyclerView != null && banners != null) {
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
            if (innerRecycler != null && products != null) {
                ProductAdapter adapter = new ProductAdapter(products);
                innerRecycler.setAdapter(adapter);
            }

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
        String titleText;
        boolean showSeeAll;

        public TitleViewHolder(@NonNull View itemView, String titleText, boolean showSeeAll) {
            super(itemView);
            this.titleText = titleText;
            this.showSeeAll = showSeeAll;
            title = itemView.findViewById(R.id.sectionTitle);
            seeAll = itemView.findViewById(R.id.seeAllRecommended);
        }

        void bind() {
            if (title != null) title.setText(titleText);
            if (seeAll != null) {
                seeAll.setVisibility(showSeeAll ? View.VISIBLE : View.GONE);
                if (showSeeAll) {
                    seeAll.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), SeeAllActivity.class);
                        intent.putExtra("category", "recommended");
                        intent.putExtra("title", titleText);
                        itemView.getContext().startActivity(intent);
                    });
                }
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
            if (product == null) return;
            name.setText(product.getName());
            price.setText("$ " + product.getPrice());
            rating.setText(String.valueOf(product.getRating()));
            soldCount.setText(String.valueOf(product.getFormattedSoldCount()));
            location.setText(product.getLocation());
            Glide.with(itemView.getContext()).load(product.getImageUrl()).into(image);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ProductDetailActivity.class);
                intent.putExtra("product", product);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}

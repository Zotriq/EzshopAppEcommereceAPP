package com.example.ezshopapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private Product product;
    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore db;
    private boolean isDescriptionExpanded = false;
    private ViewPager2 productViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        
        // 1. Get Product from Intent
        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            Toast.makeText(this, "Error: Product data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Initialize UI Components
        initUI();

        // 3. Setup Image Slider
        setupImageSlider();

        // 4. Setup Colors
        setupColorList();

        // 5. Setup Reviews
        setupReviewList();
    }

    private void initUI() {
        ImageView btnBack = findViewById(R.id.btnBack);
        productViewPager = findViewById(R.id.productViewPager);
        TextView name = findViewById(R.id.productDetailName);
        TextView price = findViewById(R.id.productDetailPrice);
        TextView rating = findViewById(R.id.productDetailRating);
        TextView soldCount = findViewById(R.id.productDetailSoldCount);
        TextView condition = findViewById(R.id.specCondition);
        TextView weight = findViewById(R.id.specWeight);
        TextView category = findViewById(R.id.specCategory);
        TextView brand = findViewById(R.id.specBrand);
        TextView description = findViewById(R.id.productDetailDescription);
        TextView btnMoreInfo = findViewById(R.id.btnMoreInfo);
        TextView storeName = findViewById(R.id.storeName);
        TextView storeLocation = findViewById(R.id.storeLocation);
        ImageView storeImage = findViewById(R.id.storeImage);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);

        // Bind Data
        name.setText(product.getName());
        price.setText("$ " + product.getPrice());
        rating.setText(String.valueOf(product.getRating()));
        soldCount.setText("|  Sold " + product.getSoldCount());
        condition.setText(": " + (product.getCondition() != null ? product.getCondition() : "New"));
        weight.setText(": " + (product.getWeight() != null ? product.getWeight() : "500 Gram"));
        category.setText(": " + product.getCategory());
        brand.setText(": " + (product.getBrand() != null ? product.getBrand() : "Brand"));
        description.setText(product.getDescription());
        storeName.setText(product.getStoreName() != null ? product.getStoreName() : "Official Store");
        storeLocation.setText(product.getLocation());

        if (product.getStoreImageUrl() != null && !product.getStoreImageUrl().isEmpty()) {
            Glide.with(this).load(product.getStoreImageUrl()).into(storeImage);
        }

        btnBack.setOnClickListener(v -> finish());
        
        // Expandable Description Logic
        btnMoreInfo.setOnClickListener(v -> {
            if (isDescriptionExpanded) {
                description.setMaxLines(3);
                btnMoreInfo.setText("More Information");
            } else {
                description.setMaxLines(Integer.MAX_VALUE);
                btnMoreInfo.setText("Less Information");
            }
            isDescriptionExpanded = !isDescriptionExpanded;
        });

        btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupImageSlider() {
        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            // Fallback to single imageUrl if list is empty
            images = new ArrayList<>();
            images.add(product.getImageUrl());
        }

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        productViewPager.setAdapter(adapter);

        // Setup dots (Optional: can add logic to update dots as user swipes)
    }

    private void setupColorList() {
        RecyclerView colorRecycler = findViewById(R.id.colorRecyclerView);
        List<String> colors = product.getColors();
        if (colors == null) colors = new ArrayList<>();
        
        ColorAdapter adapter = new ColorAdapter(colors, (hex, position) -> {
            // Sync with Image Slider: When a color is clicked, show the corresponding image
            if (product.getImageUrls() != null && position < product.getImageUrls().size()) {
                productViewPager.setCurrentItem(position, true);
            }
        });
        colorRecycler.setAdapter(adapter);
    }

    private void setupReviewList() {
        RecyclerView reviewRecycler = findViewById(R.id.reviewsRecyclerView);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewRecycler.setAdapter(reviewAdapter);
        fetchReviews();
    }

    private void fetchReviews() {
        String docId = product.getDocumentId();
        if (docId == null) return;

        db.collection("products")
                .document(docId)
                .collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }
                });
    }
}

package com.example.ezshopapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private Product product;
    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore db;
    private boolean isDescriptionExpanded = false;
    private ViewPager2 productViewPager;
    private String selectedColor;

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

        // 6. Save to Last Seen
        saveToLastSeen();
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

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void saveToLastSeen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "saveToLastSeen: User not logged in");
            return;
        }
        
        if (product.getDocumentId() == null) {
            Log.d(TAG, "saveToLastSeen: Product ID is null");
            return;
        }

        Log.d(TAG, "saveToLastSeen: Saving product " + product.getName());

        Map<String, Object> lastSeenData = new HashMap<>();
        lastSeenData.put("userId", user.getUid());
        lastSeenData.put("productId", product.getDocumentId());
        lastSeenData.put("timestamp", FieldValue.serverTimestamp());
        lastSeenData.put("name", product.getName());
        lastSeenData.put("price", product.getPrice());
        lastSeenData.put("imageUrl", product.getImageUrl());
        lastSeenData.put("rating", product.getRating());
        lastSeenData.put("soldCount", product.getSoldCount());
        lastSeenData.put("location", product.getLocation());

        db.collection("last_seen")
                .document(user.getUid() + "_" + product.getDocumentId())
                .set(lastSeenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "saveToLastSeen: Success"))
                .addOnFailureListener(e -> Log.e(TAG, "saveToLastSeen: Failed", e));
    }

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedColor == null && product.getColors() != null && !product.getColors().isEmpty()) {
            selectedColor = product.getColors().get(0);
        }

        String userId = user.getUid();
        String productId = product.getDocumentId();

        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .whereEqualTo("selectedColor", selectedColor)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Item already in cart, increment quantity
                            String docId = task.getResult().getDocuments().get(0).getId();
                            db.collection("cart").document(docId)
                                    .update("quantity", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> Toast.makeText(ProductDetailActivity.this, "Quantity updated in cart!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this, "Failed to update cart", Toast.LENGTH_SHORT).show());
                        } else {
                            // Item not in cart, add new
                            CartItem cartItem = new CartItem(
                                    userId,
                                    productId,
                                    product.getName(),
                                    product.getPrice(),
                                    product.getImageUrl(),
                                    selectedColor,
                                    1
                            );
                            db.collection("cart")
                                    .add(cartItem)
                                    .addOnSuccessListener(documentReference -> Toast.makeText(ProductDetailActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.e(TAG, "Error checking cart", task.getException());
                    }
                });
    }

    private void setupImageSlider() {
        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add(product.getImageUrl());
        }

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        productViewPager.setAdapter(adapter);
    }

    private void setupColorList() {
        RecyclerView colorRecycler = findViewById(R.id.colorRecyclerView);
        List<String> colors = product.getColors();
        if (colors == null) colors = new ArrayList<>();

        if (!colors.isEmpty()) {
            selectedColor = colors.get(0);
        }

        ColorAdapter adapter = new ColorAdapter(colors, (hex, position) -> {
            selectedColor = hex;
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

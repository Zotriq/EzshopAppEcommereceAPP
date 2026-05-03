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

    private Product product;
    private List<Review> reviewList;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore db;
    private boolean isDescriptionExpanded = false;
    private ViewPager2 productViewPager;
    private String selectedColor;
    private ImageView btnWishlist;
    private boolean isInWishlist = false;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            Toast.makeText(this, "Error: Product data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        setupImageSlider();
        setupColorList();
        setupReviewList();
        saveToLastSeen();
        checkWishlistStatus();
    }

    private void initUI() {
        ImageView btnBack = findViewById(R.id.btnBack);
        productViewPager = findViewById(R.id.productViewPager);
        TextView name = findViewById(R.id.productDetailName);
        TextView price = findViewById(R.id.productDetailPrice);
        TextView rating = findViewById(R.id.productDetailRating);
        TextView soldCountText = findViewById(R.id.productDetailSoldCount);
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
        btnWishlist = findViewById(R.id.btnWishlist);

        name.setText(product.getName());
        price.setText("$ " + product.getPrice());
        rating.setText(String.valueOf(product.getRating()));
        soldCountText.setText("|  Sold " + product.getFormattedSoldCount());
        
        condition.setText(": " + (product.getCondition() != null ? product.getCondition() : "New"));
        weight.setText(": " + (product.getWeight() != null ? product.getWeight() : "N/A"));
        category.setText(": " + product.getCategory());
        brand.setText(": " + (product.getBrand() != null ? product.getBrand() : "Brand"));
        description.setText(product.getDescription());
        storeName.setText(product.getStoreName() != null ? product.getStoreName() : "Official Store");
        storeLocation.setText(product.getLocation());

        if (product.getStoreImageUrl() != null && !product.getStoreImageUrl().isEmpty()) {
            Glide.with(this).load(product.getStoreImageUrl()).into(storeImage);
        }

        btnBack.setOnClickListener(v -> finish());

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
        btnWishlist.setOnClickListener(v -> toggleWishlist());
    }

    private void setupColorList() {
        RecyclerView colorRecycler = findViewById(R.id.colorRecyclerView);
        List<String> colors = product.getColors();
        
        if (colors == null || colors.isEmpty()) {
            findViewById(R.id.labelChooseColor).setVisibility(View.GONE);
            colorRecycler.setVisibility(View.GONE);
            return;
        }

        selectedColor = colors.get(0);

        ColorAdapter adapter = new ColorAdapter(colors, (hex, position) -> {
            selectedColor = hex;
            // Optional: if images match colors, sync them
            if (product.getImageUrls() != null && position < product.getImageUrls().size()) {
                productViewPager.setCurrentItem(position, true);
            }
        });
        
        // Ensure horizontal layout
        colorRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        colorRecycler.setAdapter(adapter);
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

    private void checkWishlistStatus() {
        if (userId == null) return;
        db.collection("wishlist").document(userId + "_" + product.getDocumentId()).get()
                .addOnSuccessListener(doc -> {
                    isInWishlist = doc.exists();
                    btnWishlist.setImageResource(isInWishlist ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                });
    }

    private void toggleWishlist() {
        if (userId == null) { Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show(); return; }
        DocumentReference docRef = db.collection("wishlist").document(userId + "_" + product.getDocumentId());
        if (isInWishlist) {
            docRef.delete().addOnSuccessListener(v -> { isInWishlist = false; btnWishlist.setImageResource(android.R.drawable.btn_star_big_off); });
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", userId); item.put("productId", product.getDocumentId());
            item.put("name", product.getName()); item.put("price", product.getPrice());
            item.put("imageUrl", product.getImageUrl());
            docRef.set(item).addOnSuccessListener(v -> { isInWishlist = true; btnWishlist.setImageResource(android.R.drawable.btn_star_big_on); });
        }
    }

    private void saveToLastSeen() {
        if (userId == null || product.getDocumentId() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId); data.put("productId", product.getDocumentId());
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("name", product.getName()); data.put("price", product.getPrice());
        data.put("imageUrl", product.getImageUrl());
        db.collection("last_seen").document(userId + "_" + product.getDocumentId()).set(data);
    }

    private void addToCart() {
        if (userId == null) { Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show(); return; }
        if (selectedColor == null && product.getColors() != null && !product.getColors().isEmpty()) selectedColor = product.getColors().get(0);
        db.collection("cart").whereEqualTo("userId", userId).whereEqualTo("productId", product.getDocumentId()).whereEqualTo("selectedColor", selectedColor).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            db.collection("cart").document(task.getResult().getDocuments().get(0).getId()).update("quantity", FieldValue.increment(1));
                        } else {
                            CartItem item = new CartItem(userId, product.getDocumentId(), product.getName(), product.getPrice(), product.getImageUrl(), selectedColor, 1);
                            db.collection("cart").add(item);
                        }
                        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

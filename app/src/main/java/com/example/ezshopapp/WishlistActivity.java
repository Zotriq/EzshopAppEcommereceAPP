package com.example.ezshopapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView wishlistRecyclerView;
    private TextView tvEmptyWishlist;
    private ProductAdapter wishlistAdapter;
    private List<Product> wishlistItems;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Please login to see your wishlist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        fetchWishlist();
    }

    private void initUI() {
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        wishlistItems = new ArrayList<>();
        wishlistAdapter = new ProductAdapter(wishlistItems);
        wishlistRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        wishlistRecyclerView.setAdapter(wishlistAdapter);
    }

    private void fetchWishlist() {
        db.collection("wishlist")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        wishlistItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getString("productId"));
                            wishlistItems.add(product);
                        }
                        wishlistAdapter.notifyDataSetChanged();
                        checkEmptyState();
                    } else {
                        Toast.makeText(this, "Error fetching wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEmptyState() {
        if (wishlistItems.isEmpty()) {
            tvEmptyWishlist.setVisibility(View.VISIBLE);
            wishlistRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyWishlist.setVisibility(View.GONE);
            wishlistRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

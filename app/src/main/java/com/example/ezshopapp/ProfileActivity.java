package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextView tvProfileName, tvProfileEmail, tvWalletBalance;
    private RecyclerView rvLastSeen;
    private Button btnLogout, btnTopUp;
    private BottomNavigationView bottomNav;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    
    private List<Product> lastSeenList;
    private RecommendationAdapter lastSeenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userId = user.getUid();

        initUI();
        setupBottomNav();
        fetchUserData();
        setupLastSeenList();
    }

    private void initUI() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        rvLastSeen = findViewById(R.id.rvLastSeen);
        btnLogout = findViewById(R.id.btnLogout);
        btnTopUp = findViewById(R.id.btnTopUp);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnTopUp.setOnClickListener(v -> {
            Toast.makeText(this, "Top-up feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnWishlist).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, WishlistActivity.class));
        });

        findViewById(R.id.btnShippingAddress).setOnClickListener(v -> Toast.makeText(this, "Shipping Address management coming soon!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnSettings).setOnClickListener(v -> Toast.makeText(this, "Account Settings coming soon!", Toast.LENGTH_SHORT).show());
        
        // --- Order Dashboard Logic ---
        findViewById(R.id.tvViewOrderHistory).setOnClickListener(v -> openOrderHistory(null));
        
        // Note: Map icons to their logical status in your database
        findViewById(R.id.layoutToPay).setOnClickListener(v -> openOrderHistory("To Pay"));
        findViewById(R.id.layoutToShip).setOnClickListener(v -> openOrderHistory("Pending"));
        findViewById(R.id.layoutToReceive).setOnClickListener(v -> openOrderHistory("Shipped"));
        findViewById(R.id.layoutToReview).setOnClickListener(v -> openOrderHistory("Completed"));
    }

    private void openOrderHistory(String filterStatus) {
        Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
        if (filterStatus != null) {
            intent.putExtra("filterStatus", filterStatus);
        }
        startActivity(intent);
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(this, ExploreActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvProfileEmail.setText(user.getEmail());
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvProfileName.setText(user.getDisplayName());
            }

            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    if (name != null) tvProfileName.setText(name);
                    
                    Double balance = documentSnapshot.getDouble("balance");
                    if (balance != null) {
                        tvWalletBalance.setText(String.format("$ %.2f", balance));
                    }
                }
            });
        }
    }

    private void setupLastSeenList() {
        lastSeenList = new ArrayList<>();
        lastSeenAdapter = new RecommendationAdapter(lastSeenList);
        rvLastSeen.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLastSeen.setAdapter(lastSeenAdapter);

        fetchLastSeen();
    }

    private void fetchLastSeen() {
        db.collection("last_seen")
                .whereEqualTo("userId", userId)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        
                        Collections.sort(docs, (d1, d2) -> {
                            java.util.Date t1 = d1.getDate("timestamp");
                            java.util.Date t2 = d2.getDate("timestamp");
                            if (t1 == null || t2 == null) return 0;
                            return t2.compareTo(t1);
                        });

                        lastSeenList.clear();
                        for (DocumentSnapshot doc : docs) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setDocumentId(doc.getString("productId"));
                                lastSeenList.add(product);
                            }
                        }
                        lastSeenAdapter.notifyDataSetChanged();
                    }
                });
    }
}

package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainRecyclerView;
    private List<Product> bestSellersList, recommendationsList;
    private List<Product> allProducts; 
    private List<String> categories;
    private List<Banner> bannerList;
    private MainHomeAdapter mainHomeAdapter;
    private FirebaseFirestore db;
    private String currentCategory = "All";
    private String currentSearchQuery = "";
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Whenever we come back to MainActivity, ensure "Home" is the highlighted tab
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        
        allProducts = new ArrayList<>();
        bestSellersList = new ArrayList<>();
        recommendationsList = new ArrayList<>();
        bannerList = new ArrayList<>();
        
        categories = Arrays.asList("All", "Laptop", "Smartphone", "Monitor", "Mouse", "Keyboard", "Headset");

        setupRecyclerView();
        fetchBanners();
        fetchProducts();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        mainRecyclerView = findViewById(R.id.mainRecyclerView);

        mainHomeAdapter = new MainHomeAdapter(
                bestSellersList, 
                recommendationsList, 
                categories, 
                bannerList,
                this::onSearch, 
                this::onCategoryClick
        );

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainHomeAdapter);
    }

    private void fetchBanners() {
        db.collection("banners")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bannerList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Banner banner = document.toObject(Banner.class);
                            bannerList.add(banner);
                        }
                        mainHomeAdapter.notifyItemChanged(1);
                    } else {
                        Toast.makeText(this, "Error getting banners: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onSearch(String query) {
        currentSearchQuery = query;
        applyFilters();
    }

    private void onCategoryClick(String category) {
        currentCategory = category;
        applyFilters();
    }

    private void applyFilters() {
        bestSellersList.clear();
        recommendationsList.clear();

        String lowerCaseQuery = currentSearchQuery.toLowerCase().trim();

        for (Product product : allProducts) {
            boolean matchesCategory = currentCategory.equals("All") || 
                                     (product.getCategory() != null && product.getCategory().equalsIgnoreCase(currentCategory));
            
            boolean matchesSearch = lowerCaseQuery.isEmpty() || 
                                    product.getName().toLowerCase().contains(lowerCaseQuery);

            if (matchesCategory && matchesSearch) {
                if (product.isBestSeller()) {
                    bestSellersList.add(product);
                }
                if (product.isRecommended()) {
                    recommendationsList.add(product);
                }
            }
        }
        mainHomeAdapter.notifyDataSetChanged();
    }

    private void fetchProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            allProducts.add(product);
                        }
                        applyFilters();
                    } else {
                        Toast.makeText(this, "Error getting products: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                return true;
            }
            return true;
        });
    }
}

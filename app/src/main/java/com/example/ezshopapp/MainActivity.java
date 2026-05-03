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
    private List<Category> categories;
    private List<Banner> bannerList;
    private MainHomeAdapter mainHomeAdapter;
    private FirebaseFirestore db;
    private String currentCategory = "All";
    private String currentSearchQuery = "";
    private BottomNavigationView bottomNav;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        checkNotifications();
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        
        allProducts = new ArrayList<>();
        bestSellersList = new ArrayList<>();
        recommendationsList = new ArrayList<>();
        bannerList = new ArrayList<>();
        
        categories = new ArrayList<>();
        categories.add(new Category("All", "")); 

        setupRecyclerView();
        fetchBanners();
        fetchCategories();
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

    private void checkNotifications() {
        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // Use ONLY userId filter to bypass Firestore Index requirements
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        boolean unreadFound = false;
                        for (QueryDocumentSnapshot doc : value) {
                            // Check both field name possibilities for robustness
                            Boolean isRead = doc.getBoolean("isRead");
                            if (isRead == null) isRead = doc.getBoolean("read");
                            
                            if (isRead != null && !isRead) {
                                unreadFound = true;
                                break;
                            }
                        }
                        if (mainHomeAdapter != null) {
                            mainHomeAdapter.setHasUnreadNotifications(unreadFound);
                        }
                    }
                });
    }

    private void fetchCategories() {
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                categories.clear();
                categories.add(new Category("All", ""));
                for (QueryDocumentSnapshot doc : value) {
                    Category category = doc.toObject(Category.class);
                    category.setDocumentId(doc.getId());
                    categories.add(category);
                }
                mainHomeAdapter.notifyItemChanged(0);
            }
        });
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
                                    (product.getName() != null && product.getName().toLowerCase().contains(lowerCaseQuery));

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
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            allProducts.add(product);
                        }
                        applyFilters();
                    }
                });
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(MainActivity.this, ExploreActivity.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return true;
        });
    }
}

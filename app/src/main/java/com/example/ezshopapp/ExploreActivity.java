package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView, trendingSearchRecyclerView, trendingProductsRecyclerView;
    private List<Product> trendingProductsList;
    private List<String> trendingSearches;
    private List<Category> categories;
    private FirebaseFirestore db;
    private RecommendationAdapter trendingProductsAdapter;
    private TrendingSearchAdapter trendingSearchAdapter;
    private CategoryAdapter categoryAdapter;
    private BottomNavigationView bottomNav;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        db = FirebaseFirestore.getInstance();

        initUI();
        setupCategories();
        setupTrendingSearches();
        setupTrendingProducts();
        setupBottomNav();
    }

    private void initUI() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        trendingSearchRecyclerView = findViewById(R.id.trendingSearchRecyclerView);
        trendingProductsRecyclerView = findViewById(R.id.trendingProductsRecyclerView);
        etSearch = findViewById(R.id.etSearch);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupCategories() {
        categories = new ArrayList<>();
        categories.add(new Category("Laptop", R.drawable.home));
        categories.add(new Category("Smartphone", android.R.drawable.ic_menu_call));
        categories.add(new Category("Monitor", android.R.drawable.ic_menu_gallery));
        categories.add(new Category("Computer", android.R.drawable.ic_menu_view));
        categories.add(new Category("Mouse", android.R.drawable.ic_menu_manage));
        categories.add(new Category("Keyboard", android.R.drawable.ic_menu_edit));

        categoryAdapter = new CategoryAdapter(categories, category -> {
            etSearch.setText(category.getName());
            performSearch(category.getName());
        });

        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryRecyclerView.setAdapter(categoryAdapter);
        categoryRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupTrendingSearches() {
        trendingSearches = new ArrayList<>();
        trendingSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        trendingSearchAdapter = new TrendingSearchAdapter(trendingSearches, query -> {
            etSearch.setText(query);
            performSearch(query);
        });
        
        trendingSearchRecyclerView.setAdapter(trendingSearchAdapter);
        fetchTrendingSearches();
    }

    private void fetchTrendingSearches() {
        db.collection("trending_searches")
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    trendingSearches.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        trendingSearches.add("Asus ROG");
                        trendingSearches.add("Macbook Air");
                        trendingSearches.add("MSI Creator");
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            trendingSearches.add(doc.getString("keyword"));
                        }
                    }
                    trendingSearchAdapter.notifyDataSetChanged();
                });
    }

    private void performSearch(String query) {
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
    }

    private void setupTrendingProducts() {
        trendingProductsList = new ArrayList<>();
        trendingProductsAdapter = new RecommendationAdapter(trendingProductsList);
        trendingProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trendingProductsRecyclerView.setAdapter(trendingProductsAdapter);
        trendingProductsRecyclerView.setNestedScrollingEnabled(false);

        // Trending Logic: soldCount >= 1000
        db.collection("products")
                .whereGreaterThanOrEqualTo("soldCount", 1000)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        trendingProductsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            trendingProductsList.add(product);
                        }
                        trendingProductsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_list);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
            }
            return true;
        });
    }
}

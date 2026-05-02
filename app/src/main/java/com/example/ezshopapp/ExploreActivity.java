package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private RecyclerView categoryRecyclerView, trendingSearchRecyclerView, trendingProductsRecyclerView, resultsRecyclerView;
    private LinearLayout discoveryContainer, resultsContainer;
    private TextView tvResultsTitle;
    
    private List<Product> trendingProductsList;
    private List<Product> allProductsList;
    private List<Product> searchResultsList;
    private List<String> trendingSearches;
    private List<Category> categories;
    
    private FirebaseFirestore db;
    private RecommendationAdapter trendingProductsAdapter;
    private RecommendationAdapter resultsAdapter;
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
        setupSearchResults();
        fetchAllProducts();
        setupSearchLogic();
        setupBottomNav();
    }

    private void initUI() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        trendingSearchRecyclerView = findViewById(R.id.trendingSearchRecyclerView);
        trendingProductsRecyclerView = findViewById(R.id.trendingProductsRecyclerView);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        
        discoveryContainer = findViewById(R.id.discoveryContainer);
        resultsContainer = findViewById(R.id.resultsContainer);
        tvResultsTitle = findViewById(R.id.tvResultsTitle);
        
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
            filterByCategory(category.getName());
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

    private void setupTrendingProducts() {
        trendingProductsList = new ArrayList<>();
        trendingProductsAdapter = new RecommendationAdapter(trendingProductsList);
        trendingProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trendingProductsRecyclerView.setAdapter(trendingProductsAdapter);
        trendingProductsRecyclerView.setNestedScrollingEnabled(false);

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

    private void setupSearchResults() {
        searchResultsList = new ArrayList<>();
        resultsAdapter = new RecommendationAdapter(searchResultsList);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(resultsAdapter);
        resultsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void fetchAllProducts() {
        allProductsList = new ArrayList<>();
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Product product = document.toObject(Product.class);
                    product.setDocumentId(document.getId());
                    allProductsList.add(product);
                }
            }
        });
    }

    private void setupSearchLogic() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            showDiscoveryMode();
            return;
        }

        showResultsMode("Search Results for \"" + query + "\"");
        searchResultsList.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (Product product : allProductsList) {
            boolean matchesName = product.getName() != null && product.getName().toLowerCase().contains(lowerQuery);
            boolean matchesStore = product.getStoreName() != null && product.getStoreName().toLowerCase().contains(lowerQuery);
            boolean matchesBrand = product.getBrand() != null && product.getBrand().toLowerCase().contains(lowerQuery);
            boolean matchesCategory = product.getCategory() != null && product.getCategory().toLowerCase().contains(lowerQuery);

            if (matchesName || matchesStore || matchesBrand || matchesCategory) {
                searchResultsList.add(product);
            }
        }
        resultsAdapter.notifyDataSetChanged();
    }

    private void filterByCategory(String categoryName) {
        showResultsMode("Category: " + categoryName);
        searchResultsList.clear();
        String lowerCategory = categoryName.toLowerCase();

        for (Product product : allProductsList) {
            if (product.getCategory() != null && product.getCategory().toLowerCase().equals(lowerCategory)) {
                searchResultsList.add(product);
            }
        }
        resultsAdapter.notifyDataSetChanged();
    }

    private void showDiscoveryMode() {
        discoveryContainer.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
    }

    private void showResultsMode(String title) {
        discoveryContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        tvResultsTitle.setText(title);
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

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_list);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainRecyclerView;
    private List<Product> bestSellersList, recommendationsList;
    private List<Product> allBestSellers, allRecommendations; // Master lists for search
    private MainHomeAdapter mainHomeAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        
        allBestSellers = new ArrayList<>();
        allRecommendations = new ArrayList<>();
        bestSellersList = new ArrayList<>();
        recommendationsList = new ArrayList<>();

        setupRecyclerView();
        fetchProducts();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        mainRecyclerView = findViewById(R.id.mainRecyclerView);

        // Pass lists and the search listener to the adapter
        mainHomeAdapter = new MainHomeAdapter(bestSellersList, recommendationsList, this::filter);

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainHomeAdapter);
    }

    private void filter(String query) {
        bestSellersList.clear();
        recommendationsList.clear();

        if (query == null || query.isEmpty()) {
            bestSellersList.addAll(allBestSellers);
            recommendationsList.addAll(allRecommendations);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : allBestSellers) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    bestSellersList.add(product);
                }
            }
            for (Product product : allRecommendations) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
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
                        allBestSellers.clear();
                        allRecommendations.clear();
                        bestSellersList.clear();
                        recommendationsList.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.isBestSeller()) {
                                allBestSellers.add(product);
                                bestSellersList.add(product);
                            }
                            if (product.isRecommended()) {
                                allRecommendations.add(product);
                                recommendationsList.add(product);
                            }
                        }
                        
                        mainHomeAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(this, "Error getting products: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }
}

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
        
        setupRecyclerView();
        fetchProducts();
        setupBottomNav();
    }

    private void setupRecyclerView() {
        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        bestSellersList = new ArrayList<>();
        recommendationsList = new ArrayList<>();

        mainHomeAdapter = new MainHomeAdapter(bestSellersList, recommendationsList);

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainHomeAdapter);
    }

    private void fetchProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bestSellersList.clear();
                        recommendationsList.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            if (product.isBestSeller()) {
                                bestSellersList.add(product);
                            }
                            if (product.isRecommended()) {
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

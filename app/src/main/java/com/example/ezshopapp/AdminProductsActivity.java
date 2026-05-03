package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private RecyclerView rvAdminProducts;
    private Button btnAddProduct;
    private FirebaseFirestore db;
    private List<Product> productList;
    private RecommendationAdapter adapter; // Using existing adapter for simplicity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        db = FirebaseFirestore.getInstance();
        rvAdminProducts = findViewById(R.id.rvAdminProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchProducts();

        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(AdminProductsActivity.this, AdminAddEditProductActivity.class));
        });
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        // In a real admin app, we'd use a specific adapter with "Edit/Delete" buttons
        // For now, let's use RecommendationAdapter and maybe add a long-click listener
        adapter = new RecommendationAdapter(productList);
        rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        rvAdminProducts.setAdapter(adapter);
    }

    private void fetchProducts() {
        db.collection("products").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                productList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Product product = doc.toObject(Product.class);
                    product.setDocumentId(doc.getId());
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
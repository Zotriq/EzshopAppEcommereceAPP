package com.example.ezshopapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SeeAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all);

        category = getIntent().getStringExtra("category");
        String title = getIntent().getStringExtra("title");

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(title != null ? title : "All Products");

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.seeAllRecyclerView);
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList);
        
        // Using Grid layout (2 columns) for the See All page
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            
                            // Filtering based on the category passed from MainActivity
                            if ("best_seller".equals(category)) {
                                if (product.isBestSeller()) {
                                    productList.add(product);
                                }
                            } else if ("recommended".equals(category)) {
                                if (product.isRecommended()) {
                                    productList.add(product);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (productList.isEmpty()) {
                            Toast.makeText(this, "No products found in this category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

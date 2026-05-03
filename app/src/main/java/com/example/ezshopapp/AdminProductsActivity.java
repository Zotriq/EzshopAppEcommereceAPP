package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private AdminProductAdapter adapter;

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
        
        // We use the custom AdminProductAdapter to control exactly what happens on click
        adapter = new AdminProductAdapter(productList, new AdminProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // When in Admin mode, clicking a product should ALWAYS open the Edit screen
                Intent intent = new Intent(AdminProductsActivity.this, AdminAddEditProductActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onProductLongClick(Product product) {
                // Optional: Show a delete/view option on long press
                showDeleteDialog(product);
            }
        });
        
        rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        rvAdminProducts.setAdapter(adapter);
    }

    private void showDeleteDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("products").document(product.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchProducts() {
        db.collection("products").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show();
                return;
            }
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
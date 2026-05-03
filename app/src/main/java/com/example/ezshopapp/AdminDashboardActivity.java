package com.example.ezshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageCategories, btnManageProducts, btnManageOrders, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnLogout = findViewById(R.id.btnLogout);

        btnManageCategories.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminCategoriesActivity.class));
        });

        btnManageProducts.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminProductsActivity.class));
        });

        btnManageOrders.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminOrdersActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }
}
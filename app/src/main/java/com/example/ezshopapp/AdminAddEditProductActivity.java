package com.example.ezshopapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddEditProductActivity extends AppCompatActivity {

    private EditText etProductName, etProductPrice, etProductCategory, etProductDescription;
    private EditText etProductImageUrl1, etProductImageUrl2, etProductImageUrl3;
    private EditText etProductColors, etProductCondition, etProductWeight, etStoreName, etBrand;
    private Button btnSaveProduct;
    private TextView tvTitle;
    private FirebaseFirestore db;
    private Product existingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_product);

        db = FirebaseFirestore.getInstance();

        tvTitle = findViewById(R.id.tvTitle);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductCategory = findViewById(R.id.etProductCategory);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductImageUrl1 = findViewById(R.id.etProductImageUrl1);
        etProductImageUrl2 = findViewById(R.id.etProductImageUrl2);
        etProductImageUrl3 = findViewById(R.id.etProductImageUrl3);
        etProductColors = findViewById(R.id.etProductColors);
        etProductCondition = findViewById(R.id.etProductCondition);
        etProductWeight = findViewById(R.id.etProductWeight);
        etStoreName = findViewById(R.id.etStoreName);
        etBrand = findViewById(R.id.etBrand);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        existingProduct = (Product) getIntent().getSerializableExtra("product");
        if (existingProduct != null) {
            tvTitle.setText("Edit Product");
            populateFields();
        }

        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void populateFields() {
        etProductName.setText(existingProduct.getName());
        etProductPrice.setText(String.valueOf(existingProduct.getPrice()));
        etProductCategory.setText(existingProduct.getCategory());
        etProductDescription.setText(existingProduct.getDescription());
        
        List<String> urls = existingProduct.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            etProductImageUrl1.setText(urls.size() > 0 ? urls.get(0) : "");
            etProductImageUrl2.setText(urls.size() > 1 ? urls.get(1) : "");
            etProductImageUrl3.setText(urls.size() > 2 ? urls.get(2) : "");
        } else {
            etProductImageUrl1.setText(existingProduct.getImageUrl());
        }
        
        if (existingProduct.getColors() != null) {
            etProductColors.setText(TextUtils.join(", ", existingProduct.getColors()));
        }
        
        etProductCondition.setText(existingProduct.getCondition());
        etProductWeight.setText(existingProduct.getWeight());
        etStoreName.setText(existingProduct.getStoreName());
        etBrand.setText(existingProduct.getBrand());
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String url1 = etProductImageUrl1.getText().toString().trim();
        String url2 = etProductImageUrl2.getText().toString().trim();
        String url3 = etProductImageUrl3.getText().toString().trim();
        String colorsStr = etProductColors.getText().toString().trim();
        String condition = etProductCondition.getText().toString().trim();
        String weight = etProductWeight.getText().toString().trim();
        String storeName = etStoreName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || url1.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Process Images
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(url1);
        if (!url2.isEmpty()) imageUrls.add(url2);
        if (!url3.isEmpty()) imageUrls.add(url3);

        // 2. Process Colors (Robust Hex parsing)
        List<String> colorList = new ArrayList<>();
        if (!colorsStr.isEmpty()) {
            String[] parts = colorsStr.split("[,\\s]+"); // Split by comma or space
            for (String part : parts) {
                String hex = part.trim();
                if (!hex.isEmpty()) {
                    if (!hex.startsWith("#")) hex = "#" + hex; // Add # if missing
                    colorList.add(hex);
                }
            }
        }

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("price", Double.parseDouble(priceStr));
        productMap.put("category", category);
        productMap.put("description", description);
        productMap.put("imageUrl", url1);
        productMap.put("imageUrls", imageUrls);
        productMap.put("colors", colorList);
        productMap.put("condition", condition);
        productMap.put("weight", weight);
        productMap.put("storeName", storeName);
        productMap.put("brand", brand);
        
        if (existingProduct == null) {
            productMap.put("rating", 4.8f);
            productMap.put("soldCount", 0);
            productMap.put("location", "Online Store");
            productMap.put("isBestSeller", false);
            productMap.put("isRecommended", true);
        }

        if (existingProduct != null) {
            db.collection("products").document(existingProduct.getDocumentId()).update(productMap)
                    .addOnSuccessListener(aVoid -> { Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show(); finish(); });
        } else {
            db.collection("products").add(productMap)
                    .addOnSuccessListener(documentReference -> { Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show(); finish(); });
        }
    }
}
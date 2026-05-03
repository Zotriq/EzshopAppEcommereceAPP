package com.example.ezshopapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
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
    private EditText etProductColors, etProductCondition, etProductWeight, etStoreName, etStoreImageUrl, etProductBrand, etProductLocation;
    private CheckBox cbIsRecommended, cbIsBestSeller;
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
        etProductBrand = findViewById(R.id.etProductBrand);
        
        etStoreName = findViewById(R.id.etStoreName);
        etStoreImageUrl = findViewById(R.id.etStoreImageUrl);
        etProductLocation = findViewById(R.id.etProductLocation);
        
        cbIsRecommended = findViewById(R.id.cbIsRecommended);
        cbIsBestSeller = findViewById(R.id.cbIsBestSeller);
        
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
        etProductBrand.setText(existingProduct.getBrand());
        etStoreName.setText(existingProduct.getStoreName());
        etStoreImageUrl.setText(existingProduct.getStoreImageUrl());
        etProductLocation.setText(existingProduct.getLocation());
        
        cbIsRecommended.setChecked(existingProduct.isRecommended());
        cbIsBestSeller.setChecked(existingProduct.isBestSeller());
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
        String brand = etProductBrand.getText().toString().trim();
        String storeName = etStoreName.getText().toString().trim();
        String storeImageUrl = etStoreImageUrl.getText().toString().trim();
        String location = etProductLocation.getText().toString().trim();
        
        boolean isRecommended = cbIsRecommended.isChecked();
        boolean isBestSeller = cbIsBestSeller.isChecked();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || url1.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Name, Price, Category, Main Image)", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        // 1. Process Images
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(url1);
        if (!url2.isEmpty()) imageUrls.add(url2);
        if (!url3.isEmpty()) imageUrls.add(url3);

        // 2. Process Colors
        List<String> colorList = new ArrayList<>();
        if (!colorsStr.isEmpty()) {
            String[] parts = colorsStr.split("[,\\s]+");
            for (String part : parts) {
                String val = part.trim();
                if (!val.isEmpty()) {
                    colorList.add(val);
                }
            }
        }

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("price", price);
        productMap.put("category", category);
        productMap.put("description", description);
        productMap.put("imageUrl", url1);
        productMap.put("imageUrls", imageUrls);
        productMap.put("colors", colorList);
        productMap.put("condition", condition);
        productMap.put("weight", weight);
        productMap.put("brand", brand);
        productMap.put("storeName", storeName);
        productMap.put("storeImageUrl", storeImageUrl);
        productMap.put("location", location);
        productMap.put("isRecommended", isRecommended);
        productMap.put("isBestSeller", isBestSeller);
        
        if (existingProduct == null) {
            productMap.put("rating", 0.0f);
            productMap.put("soldCount", 0);
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
package com.example.ezshopapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoriesActivity extends AppCompatActivity {

    private EditText etCategoryName, etCategoryImage;
    private Button btnAddCategory;
    private RecyclerView rvCategories;
    private FirebaseFirestore db;
    private List<Category> categoryList;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);

        db = FirebaseFirestore.getInstance();
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryImage = findViewById(R.id.etCategoryImage);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        rvCategories = findViewById(R.id.rvCategories);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchCategories();

        btnAddCategory.setOnClickListener(v -> addCategory());
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, category -> {
            // Optional: Implement delete on click or show dialog
            showDeleteDialog(category);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);
    }

    private void fetchCategories() {
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                categoryList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Category category = doc.toObject(Category.class);
                    category.setDocumentId(doc.getId());
                    categoryList.add(category);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addCategory() {
        String name = etCategoryName.getText().toString().trim();
        String imageUrl = etCategoryImage.getText().toString().trim();

        if (name.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = new Category(name, imageUrl);
        db.collection("categories").add(category)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                    etCategoryName.setText("");
                    etCategoryImage.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteDialog(Category category) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("categories").document(category.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
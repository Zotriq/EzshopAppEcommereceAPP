package com.example.ezshopapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private TextView tvEmptyOrders, tvOrderHistoryTitle;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private String userId;
    private String filterStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        filterStatus = getIntent().getStringExtra("filterStatus");

        if (userId == null) {
            Toast.makeText(this, "Please login to see your orders", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        fetchOrders();
    }

    private void initUI() {
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        tvOrderHistoryTitle = findViewById(R.id.tvOrderHistoryTitle);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (filterStatus != null) {
            tvOrderHistoryTitle.setText(filterStatus + " Orders");
        }

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this::showReviewDialog);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderRecyclerView.setAdapter(orderAdapter);
    }

    private void showReviewDialog(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) return;

        // For simplicity, if there are multiple items, let's review the first one 
        // or we could show a list. Let's start with the first item.
        CartItem itemToReview = order.getItems().get(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_write_review, null);
        
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etReview = view.findViewById(R.id.etReview);
        TextView tvProductName = view.findViewById(R.id.tvReviewProductName);

        tvProductName.setText("Review for: " + itemToReview.getProductName());

        builder.setView(view)
                .setTitle("Write a Review")
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String reviewText = etReview.getText().toString().trim();

                    if (reviewText.isEmpty()) {
                        Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitReview(itemToReview.getProductId(), rating, reviewText);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitReview(String productId, float rating, String reviewText) {
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        }

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        
        Review review = new Review(userName, "", date, rating, reviewText);

        db.collection("products").document(productId)
                .collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchOrders() {
        Query query = db.collection("orders")
                .whereEqualTo("userId", userId);
        
        if (filterStatus != null) {
            query = query.whereEqualTo("status", filterStatus);
        }
        
        query.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            order.setOrderId(document.getId());
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                        checkEmptyState();
                    }
                });
    }

    private void checkEmptyState() {
        if (orderList.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            orderRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            orderRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

package com.example.ezshopapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView rvAdminOrders;
    private FirebaseFirestore db;
    private List<Order> orderList;
    private AdminOrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();
        rvAdminOrders = findViewById(R.id.rvAdminOrders);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchOrders();
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(orderList, order -> {
            showStatusUpdateDialog(order);
        });
        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        rvAdminOrders.setAdapter(adapter);
    }

    private void fetchOrders() {
        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showStatusUpdateDialog(Order order) {
        String[] statuses = {"Pending", "Shipped", "Completed", "Cancelled"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    updateOrderStatus(order, newStatus);
                })
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
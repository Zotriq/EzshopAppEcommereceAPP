package com.example.ezshopapp;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        
        new AlertDialog.Builder(this)
                .setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    if (newStatus.equals("Shipped")) {
                        showRiderDetailsDialog(order);
                    } else {
                        updateOrderStatus(order, newStatus, null, null);
                    }
                })
                .show();
    }

    private void showRiderDetailsDialog(Order order) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etRiderName = new EditText(this);
        etRiderName.setHint("Rider Name");
        layout.addView(etRiderName);

        final EditText etRiderPhone = new EditText(this);
        etRiderPhone.setHint("Rider Phone Number");
        etRiderPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etRiderPhone);

        new AlertDialog.Builder(this)
                .setTitle("Shipping Details")
                .setView(layout)
                .setPositiveButton("Update & Notify", (dialog, which) -> {
                    String riderName = etRiderName.getText().toString().trim();
                    String riderPhone = etRiderPhone.getText().toString().trim();
                    updateOrderStatus(order, "Shipped", riderName, riderPhone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus, String riderName, String riderPhone) {
        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    sendNotificationToUser(order, newStatus, riderName, riderPhone);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToUser(Order order, String status, String riderName, String riderPhone) {
        String title = "Order Update";
        String message = "Your order status is now: " + status;

        if (status.equals("Shipped") && riderName != null && !riderName.isEmpty()) {
            message = "Your order has been shipped! Rider: " + riderName + " (" + riderPhone + ") is on the way.";
        } else if (status.equals("Completed")) {
            message = "Your order has been delivered successfully. Thank you for shopping with us!";
        }

        Notification notification = new Notification(order.getUserId(), title, message);
        db.collection("notifications").add(notification);
    }
}
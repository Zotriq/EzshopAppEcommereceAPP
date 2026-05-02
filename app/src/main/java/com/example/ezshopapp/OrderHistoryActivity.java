package com.example.ezshopapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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
        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderRecyclerView.setAdapter(orderAdapter);
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
                    } else {
                        // If it fails with index error, it's likely because of status filter + orderby timestamp
                        Toast.makeText(this, "Error fetching orders. Check Logcat for index link if needed.", Toast.LENGTH_LONG).show();
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

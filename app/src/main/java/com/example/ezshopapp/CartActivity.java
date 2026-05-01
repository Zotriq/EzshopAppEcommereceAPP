package com.example.ezshopapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private RecyclerView cartRecyclerView, lastSeenRecyclerView;
    private CartAdapter cartAdapter;
    private RecommendationAdapter lastSeenAdapter;
    private List<CartItem> cartItemList;
    private List<Product> lastSeenList;
    private TextView tvTotalPrice, tvEmptyCart, tvLastSeenTitle;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration cartListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Please login to see your cart", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        setupCartList();
        setupLastSeenList();
    }

    private void initUI() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        lastSeenRecyclerView = findViewById(R.id.lastSeenRecyclerView);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvLastSeenTitle = findViewById(R.id.tvLastSeenTitle);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCartList() {
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList, new CartAdapter.OnCartActionClickListener() {
            @Override
            public void onIncrease(CartItem item) {
                updateQuantity(item, 1);
            }

            @Override
            public void onDecrease(CartItem item) {
                if (item.getQuantity() > 1) {
                    updateQuantity(item, -1);
                } else {
                    deleteItem(item);
                }
            }

            @Override
            public void onDelete(CartItem item) {
                deleteItem(item);
            }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        listenToCartChanges();
    }

    private void setupLastSeenList() {
        lastSeenList = new ArrayList<>();
        lastSeenAdapter = new RecommendationAdapter(lastSeenList);
        lastSeenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lastSeenRecyclerView.setAdapter(lastSeenAdapter);

        fetchLastSeen();
    }

    private void listenToCartChanges() {
        cartListener = db.collection("cart")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        cartItemList.clear();
                        double total = 0;
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) {
                                item.setCartItemId(doc.getId());
                                cartItemList.add(item);
                                total += item.getPrice() * item.getQuantity();
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                        tvTotalPrice.setText(String.format("$ %.2f", total));
                        
                        if (cartItemList.isEmpty()) {
                            tvEmptyCart.setVisibility(View.VISIBLE);
                            cartRecyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmptyCart.setVisibility(View.GONE);
                            cartRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void fetchLastSeen() {
        // Fetch without orderBy to avoid needing a composite index in Firestore
        db.collection("last_seen")
                .whereEqualTo("userId", userId)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        
                        // Manual sort by timestamp descending
                        Collections.sort(docs, (d1, d2) -> {
                            java.util.Date t1 = d1.getDate("timestamp");
                            java.util.Date t2 = d2.getDate("timestamp");
                            if (t1 == null || t2 == null) return 0;
                            return t2.compareTo(t1);
                        });

                        lastSeenList.clear();
                        int count = 0;
                        for (DocumentSnapshot doc : docs) {
                            if (count >= 5) break;
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setDocumentId(doc.getString("productId"));
                                lastSeenList.add(product);
                                count++;
                            }
                        }
                        lastSeenAdapter.notifyDataSetChanged();
                        if (tvLastSeenTitle != null) {
                            tvLastSeenTitle.setVisibility(lastSeenList.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                        Log.d(TAG, "Fetched last seen items: " + lastSeenList.size());
                    } else {
                        Log.e(TAG, "Error fetching last seen", task.getException());
                    }
                });
    }

    private void updateQuantity(CartItem item, int change) {
        db.collection("cart").document(item.getCartItemId())
                .update("quantity", com.google.firebase.firestore.FieldValue.increment(change))
                .addOnFailureListener(e -> Toast.makeText(CartActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    private void deleteItem(CartItem item) {
        db.collection("cart").document(item.getCartItemId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartListener != null) {
            cartListener.remove();
        }
    }
}

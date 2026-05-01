package com.example.ezshopapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CartAdapter checkoutAdapter;
    private List<CartItem> checkoutItemList;
    private double subtotal = 0;
    private double discount = 0;
    private double finalTotal = 0;

    private EditText etAddress, etPromoCode;
    private TextView tvFinalTotal, btnApplyPromo;
    private RadioGroup rgPaymentMethods;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // Get data from Intent
        checkoutItemList = (List<CartItem>) getIntent().getSerializableExtra("cartItems");
        subtotal = getIntent().getDoubleExtra("totalPrice", 0);
        finalTotal = subtotal;

        if (checkoutItemList == null) {
            checkoutItemList = new ArrayList<>();
        }

        initUI();
        setupCheckoutList();
        updateTotalDisplay();
    }

    private void initUI() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        etAddress = findViewById(R.id.etAddress);
        etPromoCode = findViewById(R.id.etPromoCode);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        btnApplyPromo = findViewById(R.id.btnApplyPromo);
        rgPaymentMethods = findViewById(R.id.rgPaymentMethods);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnApplyPromo.setOnClickListener(v -> applyPromoCode());
        findViewById(R.id.btnOrderNow).setOnClickListener(v -> placeOrder());
    }

    private void setupCheckoutList() {
        // Reusing CartAdapter with isCheckoutMode = true to hide edit buttons
        checkoutAdapter = new CartAdapter(checkoutItemList, null, true);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdapter);
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for the promo code in the "promos" collection using the code as Document ID
        db.collection("promos").document(code).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Promo promo = documentSnapshot.toObject(Promo.class);
                        
                        if (promo != null) {
                            // 1. Check if the subtotal meets the minimum order requirement
                            if (subtotal >= promo.getMinOrderAmount()) {
                                
                                // 2. Calculate discount based on type: "percentage" or "fixed"
                                if ("percentage".equals(promo.getType())) {
                                    discount = subtotal * (promo.getDiscountValue() / 100);
                                } else {
                                    discount = promo.getDiscountValue();
                                }

                                finalTotal = subtotal - discount;
                                updateTotalDisplay();
                                Toast.makeText(this, "Promo Applied Successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Min order for this code is $ " + promo.getMinOrderAmount(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Invalid or Expired Promo Code", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking promo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateTotalDisplay() {
        tvFinalTotal.setText(String.format("$ %.2f", finalTotal));
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            return;
        }

        int selectedId = rgPaymentMethods.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rbSelected = findViewById(selectedId);
        // Extract only the first line of text for the payment method name
        String paymentMethod = rbSelected.getText().toString().split("\n")[0];

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("items", checkoutItemList);
        order.put("totalAmount", finalTotal);
        order.put("discountAmount", discount);
        order.put("address", address);
        order.put("paymentMethod", paymentMethod);
        order.put("status", "Pending");
        order.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CheckoutActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        Button btnDone = dialog.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            clearCartAndFinish();
        });

        dialog.show();
    }

    private void clearCartAndFinish() {
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> finish());
                });
    }
}

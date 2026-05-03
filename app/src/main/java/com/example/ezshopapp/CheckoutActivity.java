package com.example.ezshopapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
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
    private EditText etCardNumber, etCardExpiry, etCardCVV;
    private LinearLayout layoutCardDetails;
    private TextView tvFinalTotal, btnApplyPromo;
    private RadioGroup rgPaymentMethods;
    private Button btnOrderNow;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

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
        btnOrderNow = findViewById(R.id.btnOrderNow);

        layoutCardDetails = findViewById(R.id.layoutCardDetails);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCardExpiry = findViewById(R.id.etCardExpiry);
        etCardCVV = findViewById(R.id.etCardCVV);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnApplyPromo.setOnClickListener(v -> applyPromoCode());
        btnOrderNow.setOnClickListener(v -> placeOrder());

        rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCard) {
                layoutCardDetails.setVisibility(View.VISIBLE);
            } else {
                layoutCardDetails.setVisibility(View.GONE);
            }
        });
    }

    private void setupCheckoutList() {
        checkoutAdapter = new CartAdapter(checkoutItemList, null, true);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdapter);
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) return;

        db.collection("promos").document(code).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Promo promo = doc.toObject(Promo.class);
                        if (promo != null && subtotal >= promo.getMinOrderAmount()) {
                            if ("percentage".equals(promo.getType())) {
                                discount = subtotal * (promo.getDiscountValue() / 100);
                            } else {
                                discount = promo.getDiscountValue();
                            }
                            finalTotal = subtotal - discount;
                            updateTotalDisplay();
                            Toast.makeText(this, "Promo Applied!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotalDisplay() {
        tvFinalTotal.setText(String.format("$ %.2f", finalTotal));
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address required");
            return;
        }

        int selectedId = rgPaymentMethods.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedId == R.id.rbCard) {
            if (etCardNumber.getText().length() < 16) {
                etCardNumber.setError("Invalid Card Number");
                return;
            }
            if (TextUtils.isEmpty(etCardExpiry.getText())) {
                etCardExpiry.setError("Required");
                return;
            }
            if (etCardCVV.getText().length() < 3) {
                etCardCVV.setError("Invalid CVV");
                return;
            }
        }

        btnOrderNow.setEnabled(false);
        btnOrderNow.setText("Processing...");

        new Handler().postDelayed(() -> {
            RadioButton rbSelected = findViewById(selectedId);
            String paymentMethod = rbSelected.getText().toString().split("\n")[0];

            Map<String, Object> order = new HashMap<>();
            order.put("userId", userId);
            order.put("items", checkoutItemList);
            order.put("totalAmount", finalTotal);
            order.put("address", address);
            order.put("paymentMethod", paymentMethod);
            order.put("status", "Pending");
            order.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("orders").add(order)
                    .addOnSuccessListener(ref -> {
                        updateSoldCount();
                        sendOrderConfirmationNotification();
                        showSuccessDialog();
                    })
                    .addOnFailureListener(e -> {
                        btnOrderNow.setEnabled(true);
                        btnOrderNow.setText("PLACE ORDER");
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }, 2000);
    }

    private void updateSoldCount() {
        for (CartItem item : checkoutItemList) {
            db.collection("products").document(item.getProductId())
                    .update("soldCount", FieldValue.increment(item.getQuantity()));
        }
    }

    private void sendOrderConfirmationNotification() {
        if (userId == null) return;
        Notification notification = new Notification(
                userId,
                "Order Placed successfully!",
                "Hooray! Your order has been placed. Expect delivery in 2-3 business days."
        );
        db.collection("notifications").add(notification);
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
        db.collection("cart").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(snaps -> {
                    WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snaps) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(v -> finish());
                });
    }
}
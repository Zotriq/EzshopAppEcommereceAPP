package com.example.ezshopapp;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String cartItemId; // Firestore document ID
    private String userId;
    private String productId;
    private String productName;
    private double price;
    private String imageUrl;
    private String selectedColor;
    private int quantity;

    public CartItem() {
        // Required for Firestore
    }

    public CartItem(String userId, String productId, String productName, double price, String imageUrl, String selectedColor, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.selectedColor = selectedColor;
        this.quantity = quantity;
    }

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

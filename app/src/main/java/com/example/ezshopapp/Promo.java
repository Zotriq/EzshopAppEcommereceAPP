package com.example.ezshopapp;

public class Promo {
    private String code;
    private String type; // "percentage" or "fixed"
    private double discountValue;
    private double minOrderAmount;

    public Promo() {}

    public Promo(String code, String type, double discountValue, double minOrderAmount) {
        this.code = code;
        this.type = type;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
}

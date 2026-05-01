package com.example.ezshopapp;

import com.google.firebase.firestore.PropertyName;

public class Product {
    private String name;
    private double price;
    private float rating;
    private String location;
    private String imageUrl;
    private Object soldCount;
    private boolean isBestSeller;
    private boolean isRecommended;
    private String category;

    public Product() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSoldCount() { 
        return soldCount != null ? String.valueOf(soldCount) : ""; 
    }
    public void setSoldCount(Object soldCount) { this.soldCount = soldCount; }

    @PropertyName("isBestSeller")
    public boolean isBestSeller() { return isBestSeller; }

    @PropertyName("isBestSeller")
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }

    @PropertyName("isRecommended")
    public boolean isRecommended() { return isRecommended; }

    @PropertyName("isRecommended")
    public void setRecommended(boolean recommended) { isRecommended = recommended; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

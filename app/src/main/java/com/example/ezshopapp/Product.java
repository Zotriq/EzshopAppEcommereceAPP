package com.example.ezshopapp;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String documentId;
    private String name;
    private double price;
    private float rating;
    private String location;
    private String imageUrl;
    private List<String> imageUrls;
    private List<String> colors;
    private int soldCount;
    private boolean isBestSeller;
    private boolean isRecommended;
    private String category;
    private String description;
    private String condition;
    private String weight;
    private String brand;
    private String storeName;
    private String storeImageUrl;

    public Product() {}

    @Exclude
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

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

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public int getSoldCount() { 
        return soldCount; 
    }

    // Accept Object to prevent crashes during Firebase data migration
    public void setSoldCount(Object soldCount) {
        if (soldCount instanceof Number) {
            this.soldCount = ((Number) soldCount).intValue();
        } else if (soldCount instanceof String) {
            try {
                // Remove non-numeric characters (like 'k', '+', '.') then parse
                String s = ((String) soldCount).replaceAll("[^0-9]", "");
                if (!s.isEmpty()) {
                    this.soldCount = Integer.parseInt(s);
                    // If it was "1k", parsing "1" is wrong, but this prevents the crash
                    if (((String) soldCount).contains("k")) this.soldCount *= 1000;
                }
            } catch (Exception e) {
                this.soldCount = 0;
            }
        }
    }

    @Exclude
    public String getFormattedSoldCount() {
        if (soldCount >= 1000) {
            return (soldCount / 1000) + "k+";
        }
        return String.valueOf(soldCount);
    }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreImageUrl() { return storeImageUrl; }
    public void setStoreImageUrl(String storeImageUrl) { this.storeImageUrl = storeImageUrl; }
}

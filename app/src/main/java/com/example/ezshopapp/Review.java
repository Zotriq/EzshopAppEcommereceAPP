package com.example.ezshopapp;

public class Review {
    private String userName;
    private String userImageUrl;
    private String date;
    private float rating;
    private String reviewText;

    // Empty constructor for Firebase
    public Review() {}

    public Review(String userName, String userImageUrl, String date, float rating, String reviewText) {
        this.userName = userName;
        this.userImageUrl = userImageUrl;
        this.date = date;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImageUrl() { return userImageUrl; }
    public void setUserImageUrl(String userImageUrl) { this.userImageUrl = userImageUrl; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}

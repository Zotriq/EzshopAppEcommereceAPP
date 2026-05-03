package com.example.ezshopapp;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private String id;
    private String userId;
    private String title;
    private String message;
    private Date timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(String userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = new Date();
        this.isRead = false;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @PropertyName("isRead")
    public boolean isRead() { return isRead; }

    @PropertyName("isRead")
    public void setRead(boolean read) { isRead = read; }
}

package com.jakir.digitaldiary.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Note {

    @DocumentId
    private String id;
    private String title;
    private String details;
    private String date;
    private String priority;
    private boolean completed;

    @ServerTimestamp
    private Date createdAt;


    public Note() {}

    public Note(String title, String details, String date, String priority) {
        this.title = title;
        this.details = details;
        this.date = date;
        this.priority = priority;
        this.completed = false;
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getDate() { return date; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Date getCreatedAt() { return createdAt; }
}
package com.example.todolist;

public class TodoItem {
    private int id; // Unique identifier for the task
    private String title; // Task title
    private String dateTime; // Reminder date and time
    private String imageUri; // Optional: URI of the task image
    private String description; // Task description

    // Constructor for creating a new TodoItem
    public TodoItem(String title, String dateTime, String imageUri, String description) {
        this.title = title;
        this.dateTime = dateTime;
        this.imageUri = imageUri;
        this.description = description;
    }

    // Constructor for an existing TodoItem with ID
    public TodoItem(int id, String title, String dateTime, String imageUri, String description) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.imageUri = imageUri;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

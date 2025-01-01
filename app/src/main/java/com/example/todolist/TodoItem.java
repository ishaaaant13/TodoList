package com.example.todolist;

import java.io.Serializable;

public class TodoItem implements Serializable {
    private int id;
    private String title;
    private String description;
    private String imageUri; // Can be null for placeholder
    private String dateTime;

    public TodoItem(int id, String title, String description, String imageUri, String dateTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getDateTime() {
        return dateTime;
    }
}

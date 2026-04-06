package com.exemple.notetaker.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Note {
    private String id;
    private String title;
    private String content; // Markdown
    private LocalDateTime lastModified;

    public Note() {
        this.id = UUID.randomUUID().toString();
        this.title = "New Note";
        this.content = "";
        this.lastModified = LocalDateTime.now();
    }

    public Note(String title, String content) {
        this();
        this.title = title;
        this.content = content;
    }

    // Getters / Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateModified();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateModified();
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    private void updateModified() {
        this.lastModified = LocalDateTime.now();
    }
}
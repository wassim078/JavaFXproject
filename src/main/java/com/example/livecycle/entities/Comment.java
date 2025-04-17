package com.example.livecycle.entities;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Comment {
    private final IntegerProperty id;
    private final IntegerProperty postId;
    private final IntegerProperty userId;
    private final StringProperty content;
    private final StringProperty createdAt;
    private final IntegerProperty likes;
    private final IntegerProperty dislikes;

    public Comment(int id, int postId, int userId, String content, LocalDateTime createdAt, int likes, int dislikes) {
        this.id = new SimpleIntegerProperty(id);
        this.postId = new SimpleIntegerProperty(postId);
        this.userId = new SimpleIntegerProperty(userId);
        this.content = new SimpleStringProperty(content);
        this.createdAt = new SimpleStringProperty(createdAt.toString());
        this.likes = new SimpleIntegerProperty(likes);
        this.dislikes = new SimpleIntegerProperty(dislikes);
    }

    // Getters pour les propriétés
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty postIdProperty() { return postId; }
    public StringProperty contentProperty() { return content; }
    public StringProperty createdAtProperty() { return createdAt; }
    public IntegerProperty likesProperty() { return likes; }
    public IntegerProperty dislikesProperty() { return dislikes; }

    // Getters standards
    public int getId() { return id.get(); }
    public int getPostId() { return postId.get(); }
    public String getContent() { return content.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public int getLikes() { return likes.get(); }
    public int getDislikes() { return dislikes.get(); }
    public int getUserId() {
        return userId.get();
    }
}
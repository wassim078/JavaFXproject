package com.example.livecycle.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Post {
    private final IntegerProperty id;
    private final IntegerProperty forumId;
    private final IntegerProperty userId;
    private final StringProperty title;
    private final StringProperty contenu;
    private final StringProperty createdAt;
    private final DoubleProperty averageRating;

    public Post(int id, int forumId, int userId, String title, String contenu, LocalDateTime createdAt, double averageRating) {
        this.id = new SimpleIntegerProperty(id);
        this.forumId = new SimpleIntegerProperty(forumId);
        this.userId = new SimpleIntegerProperty(userId);
        this.title = new SimpleStringProperty(title);
        this.contenu = new SimpleStringProperty(contenu);
        this.createdAt = new SimpleStringProperty(createdAt.toString());
        this.averageRating = new SimpleDoubleProperty(averageRating);
    }

    // Getters pour les propriétés
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty forumIdProperty() { return forumId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty contenuProperty() { return contenu; }
    public StringProperty createdAtProperty() { return createdAt; }
    public DoubleProperty averageRatingProperty() { return averageRating; }

    // Getters standards
    public int getId() { return id.get(); }
    public int getForumId() { return forumId.get(); }
    public String getTitle() { return title.get(); }
    public String getContenu() { return contenu.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public double getAverageRating() { return averageRating.get(); }
}
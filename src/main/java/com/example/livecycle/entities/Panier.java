package com.example.livecycle.entities;

import javafx.beans.property.*;

public class Panier {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty items = new SimpleStringProperty(); // JSON { "annonceId": quantity }

    // Getters/Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }

    public String getItems() { return items.get(); }
    public void setItems(String items) { this.items.set(items); }
}
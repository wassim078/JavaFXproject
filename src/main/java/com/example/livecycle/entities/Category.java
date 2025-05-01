package com.example.livecycle.entities;
import javafx.beans.property.*;

public class Category {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();

    // Constructeurs
    public Category() {}
    public Category(int id, String name) {
        this.id.set(id);
        this.name.set(name);
    }

    // Getters/Setters Property
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }

    // Getters/Setters normaux
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }



    @Override
    public String toString() {
        return getName();
    }
}
package com.example.livecycle.entities;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CategorieCollect {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    public IntegerProperty idProperty() { return id; }
    public StringProperty nomProperty() { return nom; }
    public StringProperty descriptionProperty() { return description; }

    // Regular getters/setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }


    // Constructeurs
    public CategorieCollect() {}

    public CategorieCollect(String nom, String description) {
        this.nom.set(nom);
        this.description.set(description);
    }

    @Override
    public String toString() {
        return getNom();  // Properly returns the String value
    }
}

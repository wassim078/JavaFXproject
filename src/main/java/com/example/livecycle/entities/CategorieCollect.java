package com.example.livecycle.entities;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CategorieCollect {
    private int id;
    private String nom;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Constructeurs
    public CategorieCollect() {}

    public CategorieCollect(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    // Getters/Setters Property



    @Override
    public String toString() {
        return this.nom;
    }
}

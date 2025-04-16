package com.example.livecycle.entities;

import java.time.LocalDate;

public class Collect {
    private int id;
    private CategorieCollect categorieCollect;
    private String titre;
    private String nomProduit;
    private int quantite;
    private String lieu;
    private LocalDate dateDebut;
    private int userId;
    private String userEmail;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public CategorieCollect getCategorieCollect() {
        return categorieCollect;
    }

    public void setCategorieCollect(CategorieCollect categorieCollect) {
        this.categorieCollect = categorieCollect;
    }

    // Constructors, Getters, and Setters
    public Collect() {}

    public Collect(int id, CategorieCollect categorieCollect, String titre, String nomProduit, int quantite, String lieu, LocalDate dateDebut, int userId) {
        this.id = id;
        this.categorieCollect = categorieCollect;
        this.titre = titre;
        this.nomProduit = nomProduit;
        this.quantite = quantite;
        this.lieu = lieu;
        this.dateDebut = dateDebut;
        this.userId = userId;
    }


    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    // Getters and Setters (omitted for brevity)
}
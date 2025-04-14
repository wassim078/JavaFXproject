package com.example.livecycle.entities;
import java.time.LocalDateTime;

public class Reponse {
    private String contenu;
    private LocalDateTime date;
    private User utilisateur;

    // Constructeur
    public Reponse(String contenu, LocalDateTime date, User utilisateur) {
        this.contenu = contenu;
        this.date = date;
        this.utilisateur = utilisateur;
    }

    // Getters et setters
    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }
}

package com.example.livecycle.entities;


import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Reclamation {

    // Propriétés
    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty sujet = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleStringProperty etat = new SimpleStringProperty("En Attente");
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>(LocalDateTime.now());
    private final SimpleStringProperty file = new SimpleStringProperty();
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();
    private final List<Reponse> reponses = new ArrayList<>();

    private User userEntity;

    // Constructeurs
    public Reclamation() {
        // Constructeur par défaut nécessaire pour JPA
    }

    // Ajoutez ce constructeur à votre classe Reclamation
    public Reclamation(String sujet, String description, String type, String etat, String file, User user) {
        this.sujet.set(sujet);
        this.description.set(description);
        this.type.set(type);
        this.etat.set(etat);
        this.file.set(file);
        this.user.set(user);
    }

    // Getters/Setters Property
    public IntegerProperty idProperty() { return id; }
    public StringProperty sujetProperty() { return sujet; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty typeProperty() { return type; }
    public StringProperty etatProperty() { return etat; }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }
    public StringProperty fileProperty() { return file; }
    public ObjectProperty<User> userProperty() { return user; }

    // Getters standards
    public int getId() { return id.get(); }
    public String getSujet() { return sujet.get(); }
    public String getDescription() { return description.get(); }
    public String getType() { return type.get(); }
    public String getEtat() { return etat.get(); }
    public LocalDateTime getDate() { return date.get(); }
    public String getFile() { return file.get(); }
    public User getUser() { return user.get(); }
    public List<Reponse> getReponses() { return reponses; }

    // Setters standards
    public void setId(int id) { this.id.set(id); }
    public void setSujet(String sujet) { this.sujet.set(sujet); }
    public void setDescription(String description) { this.description.set(description); }
    public void setType(String type) { this.type.set(type); }
    public void setEtat(String etat) { this.etat.set(etat); }
    public void setDate(LocalDateTime date) { this.date.set(date); }
    public void setFile(String file) { this.file.set(file); }
    public void setUser(User user) { this.user.set(user); }

    // Méthode pour obtenir la date formatée
    public String getDateString() {
        if (date.get() == null) return "N/A";
        return date.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Méthodes utilitaires
    public void addReponse(Reponse reponse) {
        this.reponses.add(reponse);
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + getId() +
                ", sujet='" + getSujet() + '\'' +
                ", type='" + getType() + '\'' +
                ", etat='" + getEtat() + '\'' +
                ", date=" + getDateString() +
                '}';
    }
}
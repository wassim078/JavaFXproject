package com.example.livecycle.entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONArray;

public class User {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty prenom = new SimpleStringProperty();
    private SimpleStringProperty nom = new SimpleStringProperty();
    private SimpleStringProperty email = new SimpleStringProperty();
    private SimpleStringProperty password = new SimpleStringProperty();
    private SimpleStringProperty adresse = new SimpleStringProperty();
    private SimpleStringProperty telephone = new SimpleStringProperty();
    private SimpleStringProperty roles = new SimpleStringProperty();
    private SimpleStringProperty image = new SimpleStringProperty();
    private static final String UPLOADS_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String DEFAULT_AVATAR = "/com/example/livecycle/images/default-avatar.png";
    // Constructeur

    public User() {
        // Initialize properties if needed
    }
    public String getImagePath() {
        if (image.get() == null || image.get().isEmpty()) {
            return DEFAULT_AVATAR;
        }
        return UPLOADS_DIR + image.get(); // Use the full path
    }
    public String getEffectiveImagePath() {
        String img = getImage();
        if (img == null || img.isEmpty()) {
            return DEFAULT_AVATAR;
        }
        return img.startsWith("http") ? img : UPLOADS_DIR + img;
    }




    public User(String prenom, String nom, String email,
                String password, String adresse, String telephone, String roles, String image) {
        this.prenom.set(prenom);
        this.nom.set(nom);
        this.email.set(email);
        this.password.set(password);
        this.adresse.set(adresse);
        this.telephone.set(telephone);
        this.roles.set(roles);
        this.image.set(image);
    }
    // Add property getters
    public StringProperty prenomProperty() { return prenom; }
    public StringProperty nomProperty() { return nom; }
    public StringProperty emailProperty() { return email; }
    public StringProperty adresseProperty() { return adresse; }
    public StringProperty telephoneProperty() { return telephone; }
    public StringProperty rolesProperty() { return roles; }
    public StringProperty imageProperty() { return image; }
    // Getters
    public int getId() { return id.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getNom() { return nom.get(); }
    public String getEmail() { return email.get(); }
    public String getPassword() { return password.get(); }
    public String getAdresse() { return adresse.get(); }
    public String getTelephone() { return telephone.get(); }
    public String getRoles() { return roles.get(); }
    public String getImage() { return image.get(); }

    public void setId(int id) { this.id.set(id); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setNom(String nom) { this.nom.set(nom); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPassword(String password) { this.password.set(password); }
    public void setAdresse(String adresse) { this.adresse.set(adresse); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setRoles(String roles) { this.roles.set(roles); }
    public void setImage(String image) { this.image.set(image); }

    public String getFormattedRoles() {
        try {
            JSONArray rolesArray = new JSONArray(getRoles());
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < rolesArray.length(); i++) {
                String role = rolesArray.getString(i).replace("ROLE_", "");
                if (i > 0) formatted.append(", ");
                formatted.append(role);
            }
            return formatted.toString();
        } catch (Exception e) {
            return "User";
        }
    }
}

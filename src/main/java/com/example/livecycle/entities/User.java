package com.example.livecycle.entities;

import javafx.beans.property.*;
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
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty(false);
    private SimpleStringProperty verificationToken = new SimpleStringProperty();
    private SimpleBooleanProperty isBanned = new SimpleBooleanProperty(false);
    private SimpleObjectProperty<byte[]> faceEncoding = new SimpleObjectProperty<>();
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
        this.enabled.set(false);
        this.verificationToken.set("");
        this.faceEncoding.set(null);
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
    public void setTelephone(String telephone) {
        // Remove all non-digits
        String cleaned = telephone.replaceAll("[^0-9]", "");

        // Handle Tunisian numbers
        if (cleaned.length() == 8) { // Local format (e.g., 23189557)
            cleaned = "216" + cleaned; // ➡️ Becomes 21623189557
        } else if (cleaned.startsWith("0")) { // Local format with leading 0 (e.g., 023189557)
            cleaned = "216" + cleaned.substring(1); // ➡️ 21623189557
        }

        this.telephone.set(cleaned);
    }
    public void setRoles(String roles) { this.roles.set(roles); }
    public void setImage(String image) { this.image.set(image); }




    public void setEnabled(boolean enabled) { this.enabled.set(enabled); }
    public boolean isEnabled() { return enabled.get(); }
    public String getVerificationToken() { return verificationToken.get(); }
    public void setVerificationToken(String token) { verificationToken.set(token); }




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




    public boolean isBanned() { return isBanned.get(); }
    public void setBanned(boolean banned) { isBanned.set(banned); }
    public SimpleBooleanProperty bannedProperty() { return isBanned; }



    public byte[] getFaceEncoding() {
        return faceEncoding.get();
    }

    public void setFaceEncoding(byte[] faceEncoding) {
        this.faceEncoding.set(faceEncoding);
    }

    public SimpleObjectProperty<byte[]> faceEncodingProperty() {
        return faceEncoding;
    }

}

package com.example.livecycle.entities;
import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Annonce {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty titre = new SimpleStringProperty();
    private final DoubleProperty poids = new SimpleDoubleProperty();
    private final DoubleProperty prix = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final ObjectProperty<Category> categorieAnnonce = new SimpleObjectProperty<>();
    private transient String userEmail;


    // Constructeurs
    public Annonce() {}

    // Getters/Setters Property
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty titreProperty() { return titre; }
    public DoubleProperty poidsProperty() { return poids; }
    public DoubleProperty prixProperty() { return prix; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty imageProperty() { return image; }
    public IntegerProperty quantiteProperty() { return quantite; }
    public ObjectProperty<Category> categorieAnnonceProperty() { return categorieAnnonce; }
    private final IntegerProperty categorieAnnonceId = new SimpleIntegerProperty();


    // Getters/Setters normaux
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }

    public String getTitre() { return titre.get(); }
    public void setTitre(String titre) { this.titre.set(titre); }

    public double getPoids() { return poids.get(); }
    public void setPoids(double poids) { this.poids.set(poids); }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getImage() { return image.get(); }
    public void setImage(String image) { this.image.set(image); }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }


    public Category getCategorieAnnonce() { return categorieAnnonce.get(); }

    public void setCategorieAnnonce(Category category) {
        this.categorieAnnonce.set(category);
        this.categorieAnnonceId.set(category.getId());
    }

    public int getCategorieAnnonceId() { return categorieAnnonceId.get(); }
    public void setCategorieAnnonceId(int id) { this.categorieAnnonceId.set(id); }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

}
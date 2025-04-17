package com.example.livecycle.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Commande {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();

    private final StringProperty clientName = new SimpleStringProperty();
    private final StringProperty clientFamilyName = new SimpleStringProperty();
    private final StringProperty clientAddress = new SimpleStringProperty();
    private final StringProperty clientPhone = new SimpleStringProperty();
    private final StringProperty annonceQuantities = new SimpleStringProperty();
    private final StringProperty methodePaiement = new SimpleStringProperty();
    private final StringProperty etatCommande = new SimpleStringProperty(); // Ajout manquant
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private final StringProperty instructionSpeciale = new SimpleStringProperty();
    private final DoubleProperty prixTotal = new SimpleDoubleProperty();

    // Constructeurs
    public Commande() {}

    public Commande(int id, String clientName, String clientFamilyName,
                    String clientAddress, String clientPhone,
                    String annonceQuantities, String methodePaiement,
                    String etatCommande, LocalDateTime date,
                    String instructionSpeciale, double prixTotal) {
        setId(id);
        setClientName(clientName);
        setClientFamilyName(clientFamilyName);
        setClientAddress(clientAddress);
        setClientPhone(clientPhone);
        setAnnonceQuantities(annonceQuantities);
        setMethodePaiement(methodePaiement);
        setEtatCommande(etatCommande);
        setDate(date);
        setInstructionSpeciale(instructionSpeciale);
        setPrixTotal(prixTotal);
    }

    // Getters/Setters complets
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }


    public String getClientName() { return clientName.get(); }
    public void setClientName(String value) { clientName.set(value); }
    public StringProperty clientNameProperty() { return clientName; }

    public String getClientFamilyName() { return clientFamilyName.get(); }
    public void setClientFamilyName(String value) { clientFamilyName.set(value); }
    public StringProperty clientFamilyNameProperty() { return clientFamilyName; }

    public String getClientAddress() { return clientAddress.get(); }
    public void setClientAddress(String value) { clientAddress.set(value); }
    public StringProperty clientAddressProperty() { return clientAddress; }

    public String getClientPhone() { return clientPhone.get(); }
    public void setClientPhone(String value) { clientPhone.set(value); }
    public StringProperty clientPhoneProperty() { return clientPhone; }

    public String getAnnonceQuantities() { return annonceQuantities.get(); }
    public void setAnnonceQuantities(String value) { annonceQuantities.set(value); }
    public StringProperty annonceQuantitiesProperty() { return annonceQuantities; }

    public String getMethodePaiement() { return methodePaiement.get(); }
    public void setMethodePaiement(String value) { methodePaiement.set(value); }
    public StringProperty methodePaiementProperty() { return methodePaiement; }

    public String getEtatCommande() { return etatCommande.get(); } // Getter ajouté
    public void setEtatCommande(String value) { etatCommande.set(value); } // Setter ajouté
    public StringProperty etatCommandeProperty() { return etatCommande; }

    public LocalDateTime getDate() { return date.get(); }
    public void setDate(LocalDateTime value) { date.set(value); }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }

    public String getInstructionSpeciale() { return instructionSpeciale.get(); }
    public void setInstructionSpeciale(String value) { instructionSpeciale.set(value); }
    public StringProperty instructionSpecialeProperty() { return instructionSpeciale; }

    public double getPrixTotal() { return prixTotal.get(); }
    public void setPrixTotal(double value) { prixTotal.set(value); }
    public DoubleProperty prixTotalProperty() { return prixTotal; }

    @Override
    public String toString() {
        return String.format("Commande [ID=%d, Client=%s %s, Total=%.2f DH, État=%s]",
                getId(),
                getClientName(),
                getClientFamilyName(),
                getPrixTotal(),
                getEtatCommande());
    }
}
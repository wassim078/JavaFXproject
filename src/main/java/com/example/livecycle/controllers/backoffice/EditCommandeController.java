package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Commande;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class EditCommandeController {

    @FXML private TextField clientField;
    @FXML private TextArea adresseField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> paiementCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField totalField;

    private Commande currentCommande;
    private Consumer<Commande> saveHandler;

    @FXML
    public void initialize() {
        setupPaymentMethods();
    }

    public void setCommande(Commande commande) {
        this.currentCommande = commande;
        populateFields(commande);
    }

    public void setSaveHandler(Consumer<Commande> saveHandler) {
        this.saveHandler = saveHandler;
    }

    private void setupPaymentMethods() {
        paiementCombo.getItems().addAll("Espèce", "Carte bancaire", "Chèque", "Virement");
    }

    private void populateFields(Commande commande) {
        if(commande != null) {
            clientField.setText(commande.getClientName() + " " + commande.getClientFamilyName());
            adresseField.setText(commande.getClientAddress());
            telephoneField.setText(commande.getClientPhone());
            paiementCombo.getSelectionModel().select(commande.getMethodePaiement());
            datePicker.setValue(commande.getDate().toLocalDate());
            totalField.setText(String.format("%.2f", commande.getPrixTotal()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            currentCommande = currentCommande != null ? currentCommande : new Commande();

            // Validation
            if(datePicker.getValue() == null) {
                showError("Date requise", "La date est obligatoire");
                return;
            }

            // Split client name
            String[] names = clientField.getText().split(" ", 2);

            // Update fields
            currentCommande.setClientName(names[0]);
            currentCommande.setClientFamilyName(names.length > 1 ? names[1] : "");
            currentCommande.setClientAddress(adresseField.getText());
            currentCommande.setClientPhone(telephoneField.getText());
            currentCommande.setMethodePaiement(paiementCombo.getValue());
            currentCommande.setDate(LocalDateTime.of(datePicker.getValue(), LocalTime.now()));
            currentCommande.setPrixTotal(Double.parseDouble(totalField.getText()));

            if(saveHandler != null) {
                saveHandler.accept(currentCommande);
            }

            closeDialog();

        } catch (NumberFormatException e) {
            showError("Format invalide", "Le total doit être un nombre valide");
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        clientField.getScene().getWindow().hide();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showEditCommandeDialog(Commande commande, Consumer<Commande> onSave) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/edit_commande.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());

            EditCommandeController controller = loader.getController();
            controller.setCommande(commande);
            controller.setSaveHandler(updatedCommande -> {
                onSave.accept(updatedCommande); // callback pour traitement
                // Rafraîchir la TableView ou autre action après sauvegarde
            });

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.handleSave(); // Valide les changements
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
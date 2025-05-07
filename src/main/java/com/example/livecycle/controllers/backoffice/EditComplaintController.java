package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Reclamation;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.ReclamationDAO;
import com.example.livecycle.services.SMSService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;

public class EditComplaintController {
    @FXML private TextField subjectField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> etatComboBox;
    @FXML private Hyperlink fileLink;

    private Reclamation currentReclamation;
    private ReclamationManagementController parentController;
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();

    public void initialize() {
        etatComboBox.getItems().addAll("En Attente", "En Cours", "Résolue", "Rejetée");
    }

    public void setReclamation(Reclamation reclamation, ReclamationManagementController parent) {
        this.currentReclamation = reclamation;
        this.parentController = parent;

        subjectField.setText(reclamation.getSujet());
        descriptionField.setText(reclamation.getDescription());
        typeComboBox.setValue(reclamation.getType());
        etatComboBox.setValue(reclamation.getEtat());

        setupFileLink(reclamation.getFile());
    }

    private void setupFileLink(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            fileLink.setText(new File(filePath).getName());
            fileLink.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (Exception ex) {
                    parentController.showAlert("Error", "Could not open file: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        } else {
            fileLink.setText("No files attached");
            fileLink.setDisable(true);
        }
    }

    @FXML
    private void handleSave() {
        String newEtat = etatComboBox.getValue();
        currentReclamation.setEtat(newEtat);

        if (reclamationDAO.updateReclamation(currentReclamation)) {
            // Send WhatsApp notification
            User user = currentReclamation.getUser();
            String userPhone = user.getTelephone();
            String userName = user.getNom();

            if (userPhone != null && !userPhone.isEmpty()) {
                String message = String.format(
                        "Bonjour %s,\nVotre réclamation \"%s\" a été mise à jour.\nNouveau statut : %s.",
                        userName, currentReclamation.getSujet(), newEtat
                );
                SMSService.sendSMS(userPhone, message);

            }

            parentController.refreshTable();
            closeWindow();
        } else {
            parentController.showAlert("Error", "Failed to update complaint", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) subjectField.getScene().getWindow()).close();
    }
}
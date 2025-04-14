package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Reclamation;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.ReclamationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;

public class CreateComplaintController {
    @FXML private TextField subjectField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Label fileNameLabel;
    @FXML private Label subjectError;
    @FXML private Label descriptionError;
    private User currentUser;
    private File selectedFile;
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();

    public void initialize() {
        typeComboBox.getItems().addAll(
                "Technical Issue",
                "Billing Problem",
                "Service Complaint",
                "Other"
        );
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleFileBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        Reclamation newComplaint = new Reclamation(
                subjectField.getText(),
                descriptionField.getText(),
                typeComboBox.getValue(),
                "En Attente",  // Default status
                selectedFile != null ? selectedFile.getAbsolutePath() : "",
                currentUser
        );

        try {
            boolean success = reclamationDAO.addReclamation(newComplaint);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Complaint submitted successfully!");
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit complaint!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Reset errors
        subjectError.setVisible(false);
        descriptionError.setVisible(false);
        subjectField.getStyleClass().remove("error");
        descriptionField.getStyleClass().remove("error");

        // Validate subject
        if (subjectField.getText().trim().isEmpty()) {
            subjectError.setText("Subject field is required");
            subjectError.setVisible(true);
            subjectField.getStyleClass().add("error");
            isValid = false;
        }

        // Validate description
        if (descriptionField.getText().trim().isEmpty()) {
            descriptionError.setText("Description field is required");
            descriptionError.setVisible(true);
            descriptionField.getStyleClass().add("error");
            isValid = false;
        }

        // Validate type
        if (typeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a complaint type");
            isValid = false;
        }

        return isValid;
    }




    private void clearForm() {
        // Clear errors when form is reset
        subjectError.setVisible(false);
        descriptionError.setVisible(false);
        subjectField.getStyleClass().remove("error");
        descriptionField.getStyleClass().remove("error");

        // Rest of your existing clear code
        subjectField.clear();
        descriptionField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        fileNameLabel.setText("");
        selectedFile = null;
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
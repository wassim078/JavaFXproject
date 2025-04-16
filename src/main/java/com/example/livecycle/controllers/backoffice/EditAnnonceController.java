package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;

public class EditAnnonceController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField weightField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ImageView imagePreview;

    private Annonce annonce;
    private Runnable refreshCallback;
    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();
    private File selectedImageFile;

    // Updated method to accept both annonce and callback
    public void setAnnonce(Annonce annonce, Runnable refreshCallback) {
        this.annonce = annonce;
        this.refreshCallback = refreshCallback;
        populateFields();
        loadCategories();
    }

    private void populateFields() {
        titleField.setText(annonce.getTitre());
        descriptionField.setText(annonce.getDescription());
        weightField.setText(String.valueOf(annonce.getPoids()));
        priceField.setText(String.valueOf(annonce.getPrix()));
        quantityField.setText(String.valueOf(annonce.getQuantite()));

        if (annonce.getImage() != null) {
            File file = new File(annonce.getImage());
            if (file.exists()) {
                imagePreview.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    private void loadCategories() {
        try {
            categoryCombo.getItems().clear();
            categoryCombo.getItems().addAll(categoryService.recuperer());
            categoryCombo.getSelectionModel().select(annonce.getCategorieAnnonce());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            annonce.setTitre(titleField.getText());
            annonce.setDescription(descriptionField.getText());
            annonce.setPoids(Double.parseDouble(weightField.getText()));
            annonce.setPrix(Double.parseDouble(priceField.getText()));
            annonce.setQuantite(Integer.parseInt(quantityField.getText()));

            // Update image only if a new one was selected
            if (selectedImageFile != null) {
                annonce.setImage(selectedImageFile.getAbsolutePath());
            }

            if (annonceService.modifier(annonce)) {
                refreshCallback.run();
                closeWindow();
            }
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", "Check input values: " + e.getMessage());
        }
    }


    private void validateInputs() throws IllegalArgumentException {
        if (titleField.getText().trim().isEmpty() ||
                descriptionField.getText().trim().isEmpty() ||
                categoryCombo.getValue() == null) {
            throw new IllegalArgumentException("All required fields must be filled");
        }

        try {
            Double.parseDouble(weightField.getText());
            Double.parseDouble(priceField.getText());
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        // Get the current stage using any control (e.g., imagePreview)
        Stage stage = (Stage) imagePreview.getScene().getWindow();
        stage.close();
    }
}
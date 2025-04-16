package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

public class EditAnnonceController {
    @FXML private TextField titleField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextArea descriptionField;
    @FXML private VBox formContainer;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField poidsField;
    @FXML private ImageView imagePreview;

    private Annonce annonce;
    private Runnable refreshCallback;
    private final AnnonceService annonceService = new AnnonceService();
    private File selectedImageFile;
    private final CategoryService categoryService = new CategoryService();




    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
        populateFields();
        loadCategories();
    }
    private void loadCategories() {
        try {
            categoryCombo.getItems().addAll(categoryService.recuperer());
            categoryCombo.setValue(annonce.getCategorieAnnonce()); // Set current category
        } catch (SQLException e) {
            showError("Error loading categories: " + e.getMessage());
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void populateFields() {
        titleField.setText(annonce.getTitre());
        poidsField.setText(String.valueOf(annonce.getPoids())); // Populate weight
        priceField.setText(String.valueOf(annonce.getPrix()));
        quantityField.setText(String.valueOf(annonce.getQuantite()));
        descriptionField.setText(annonce.getDescription());

        // Load existing image
        if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
            File file = new File(annonce.getImage());
            if (file.exists()) {
                imagePreview.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    @FXML
    private void handleBrowseImage() {
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
            // Validate inputs
            validateInputs();

            // Update announcement object
            annonce.setTitre(titleField.getText());
            annonce.setCategorieAnnonce(categoryCombo.getValue());
            annonce.setPoids(Double.parseDouble(poidsField.getText()));
            annonce.setPrix(Double.parseDouble(priceField.getText()));
            annonce.setQuantite(Integer.parseInt(quantityField.getText()));
            annonce.setDescription(descriptionField.getText());

            // Update image if selected
            if (selectedImageFile != null) {
                annonce.setImage(selectedImageFile.getAbsolutePath());
            }

            // Save to database
            if (annonceService.modifier(annonce)) {
                refreshCallback.run(); // Refresh the grid
                ((GridPane) formContainer.getParent()).getChildren().clear(); // Close form
            }
        } catch (IllegalArgumentException | SQLException e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }

    private void validateInputs() {
        if (titleField.getText().trim().isEmpty() ||
                poidsField.getText().trim().isEmpty() ||
                priceField.getText().trim().isEmpty() ||
                quantityField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required!");
        }

        try {
            Double.parseDouble(poidsField.getText());
            Double.parseDouble(priceField.getText());
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values!");
        }
    }
}
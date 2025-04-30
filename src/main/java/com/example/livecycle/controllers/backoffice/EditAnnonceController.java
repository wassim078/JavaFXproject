package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CategoryAnnonceService;
import com.example.livecycle.services.UserService;
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
    private final CategoryAnnonceService categoryAnnonceService = new CategoryAnnonceService();
    private final UserService userService = new UserService();
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
            categoryCombo.getItems().addAll(categoryAnnonceService.recuperer());
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
        if (validateInputs()) {
            try {
                // Track changes
                StringBuilder changes = new StringBuilder();
                
                if (!annonce.getTitre().equals(titleField.getText())) {
                    changes.append("Title: ").append(annonce.getTitre()).append(" → ").append(titleField.getText()).append("\n");
                }
                if (!annonce.getDescription().equals(descriptionField.getText())) {
                    changes.append("Description: Modified\n");
                }
                if (annonce.getPoids() != Double.parseDouble(weightField.getText())) {
                    changes.append("Weight: ").append(annonce.getPoids()).append(" → ").append(weightField.getText()).append("\n");
                }
                if (annonce.getPrix() != Double.parseDouble(priceField.getText())) {
                    changes.append("Price: ").append(annonce.getPrix()).append(" → ").append(priceField.getText()).append("\n");
                }
                if (annonce.getQuantite() != Integer.parseInt(quantityField.getText())) {
                    changes.append("Quantity: ").append(annonce.getQuantite()).append(" → ").append(quantityField.getText()).append("\n");
                }
                if (annonce.getCategorieAnnonce().getId() != categoryCombo.getValue().getId()) {
                    changes.append("Category: ").append(annonce.getCategorieAnnonce().getName())
                            .append(" → ").append(categoryCombo.getValue().getName()).append("\n");
                }
                if (selectedImageFile != null) {
                    changes.append("Image: Updated\n");
                }

                // Update announcement
                annonce.setTitre(titleField.getText());
                annonce.setDescription(descriptionField.getText());
                annonce.setPoids(Double.parseDouble(weightField.getText()));
                annonce.setPrix(Double.parseDouble(priceField.getText()));
                annonce.setQuantite(Integer.parseInt(quantityField.getText()));
                
                Category selectedCategory = categoryCombo.getValue();
                if (selectedCategory != null) {
                    annonce.setCategorieAnnonce(selectedCategory);
                }

                if (selectedImageFile != null) {
                    annonce.setImage(selectedImageFile.getAbsolutePath());
                }

                if (annonceService.modifier(annonce)) {
                    // Send notification if there were changes
                    if (changes.length() > 0) {
                        String notificationMessage = "Your announcement '" + annonce.getTitre() + "' has been modified by an administrator.\nChanges:\n" + changes.toString();
                        userService.addNotification(annonce.getUserId(), notificationMessage);
                    }
                    
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    closeWindow();
                }
            } catch (SQLException | IllegalArgumentException e) {
                showAlert("Save Error", "Failed to save announcement: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() throws IllegalArgumentException {
        if (titleField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (categoryCombo.getValue() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        try {
            double weight = Double.parseDouble(weightField.getText());
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be greater than 0");
            }
            
            double price = Double.parseDouble(priceField.getText());
            if (price <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0");
            }
            
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values");
        }

        return true;
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
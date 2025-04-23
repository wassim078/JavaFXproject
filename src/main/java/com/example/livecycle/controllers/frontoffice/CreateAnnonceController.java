// CreateAnnonceController.java
package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CategoryAnnonceService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class CreateAnnonceController {
    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField weightField;
    @FXML private TextField quantityField;
    @FXML private ImageView imagePreview;
    @FXML private Label placeholderLabel;
    private File imageFile;
    private User user;
    private Runnable refreshCallback;
    private final AnnonceService annonceService = new AnnonceService();
    private Runnable backToShopCallback;
    private UserDashboardController dashboardController;
    private Runnable backCallback;


    public void setBackToShopCallback(Runnable callback) {
        this.backToShopCallback = callback;
    }
    public void setDashboardController(UserDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void initialize() {
        placeholderLabel.setVisible(true);

        imagePreview.imageProperty().addListener((obs, oldVal, newVal) -> {
            placeholderLabel.setVisible(newVal == null);
        });
    }

    @FXML
    private void handleBack() {
        if (backCallback != null) {
            backCallback.run(); // Restore the original view
        }
    }
    public void setBackCallback(Runnable callback) {
        this.backCallback = callback;
    }

    public void setUser(User user) {
        this.user = user;
        initializeCategories();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void initializeCategories() {
        try {
            List<Category> categories = new CategoryAnnonceService().recuperer();
            categoryCombo.getItems().addAll(categories);

            // Add cell factory to center text
            categoryCombo.setCellFactory(lv -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.toString());
                        setAlignment(Pos.CENTER_LEFT); // Force text alignment
                    }
                }
            });
        } catch (SQLException e) {
            showError("Error loading categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imageFile = selectedFile;
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        try {
            validateInputs();

            Annonce annonce = new Annonce();
            annonce.setUserId(user.getId());
            annonce.setTitre(titleField.getText().trim());
            annonce.setCategorieAnnonce(categoryCombo.getValue());
            annonce.setDescription(descriptionField.getText().trim());
            annonce.setPrix(Double.parseDouble(priceField.getText()));
            annonce.setPoids(Double.parseDouble(weightField.getText()));
            annonce.setQuantite(Integer.parseInt(quantityField.getText()));

            if (imageFile != null) {
                annonce.setImage(imageFile.getAbsolutePath());
            } else {
                throw new IllegalArgumentException("Please select an image");
            }

            if (annonceService.ajouter(annonce)) {
                showSuccess("Announcement created successfully!\nYou can view it in your list.");

                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                if (backCallback != null) {
                    backCallback.run();
                }
            }
        } catch (IllegalArgumentException | SQLException e) {
            showError("Validation Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Remove form from parent
        imagePreview.getScene().getWindow().hide();
    }
    private void   validateInputs() throws IllegalArgumentException {
        if (titleField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (categoryCombo.getValue() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (imageFile == null) {
            throw new IllegalArgumentException("Image is required");
        }

        try {
            Double.parseDouble(priceField.getText());
            Double.parseDouble(weightField.getText());
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric values");
        }
    }
    private void showError(String message) {
        new Alert (Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Operation Successful");
        alert.setContentText(message);

        // Add custom style class
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("success-dialog");

        alert.showAndWait();
    }
}
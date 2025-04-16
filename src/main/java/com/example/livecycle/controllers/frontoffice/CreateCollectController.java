package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CategorieCollectService;
import com.example.livecycle.services.CollectService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateCollectController {
    @FXML private ComboBox<CategorieCollect> categoryCombo;
    @FXML private TextField titleField;
    @FXML private TextField productField;
    @FXML private TextField quantityField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;

    @FXML private Label categoryError;
    @FXML private Label titleError;
    @FXML private Label productError;
    @FXML private Label quantityError;
    @FXML private Label locationError;
    @FXML private Label dateError;
    @FXML private Label formError;

    private User currentUser;
    private final CollectService collectService = new CollectService();
    private final CategorieCollectService categorieService = new CategorieCollectService();

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryCombo.getItems().addAll(categorieService.recuperer());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleCreate() {
        clearErrors();
        List<String> errors = new ArrayList<>();

        // Validate inputs
        if (categoryCombo.getValue() == null) {
            errors.add("Category");
            categoryError.setText("Required");
        }

        // Title validation (trimmed and starts with letter)
        String titleInput = titleField.getText().trim();
        if (titleInput.isEmpty()) {
            errors.add("Title");
            titleError.setText("Required");
        } else {
            if (!Character.isLetter(titleInput.charAt(0))) {
                errors.add("Title");
                titleError.setText("Must start with a letter");
            }
        }

        // Rest of the validations remain the same...
        if (productField.getText().isEmpty()) {
            errors.add("Product");
            productError.setText("Required");
        }
        if (quantityField.getText().isEmpty()) {
            errors.add("Quantity");
            quantityError.setText("Required");
        } else {
            try {
                Integer.parseInt(quantityField.getText());
            } catch (NumberFormatException e) {
                errors.add("Quantity");
                quantityError.setText("Must be a number");
            }
        }
        if (locationField.getText().isEmpty()) {
            errors.add("Location");
            locationError.setText("Required");
        }
        if (datePicker.getValue() == null) {
            errors.add("Date");
            dateError.setText("Required");
        }

        if (!errors.isEmpty()) {
            formError.setVisible(true);
            formError.setText("Please fix the following fields: " + String.join(", ", errors));
            return;
        }

        // Proceed with creation
        Collect collect = new Collect();
        collect.setCategorieCollect(categoryCombo.getValue());
        collect.setTitre(titleField.getText().trim()); // Save trimmed title
        collect.setNomProduit(productField.getText());
        collect.setQuantite(Integer.parseInt(quantityField.getText()));
        collect.setLieu(locationField.getText());
        collect.setDateDebut(datePicker.getValue());
        collect.setUserId(currentUser.getId());

        try {
            if (collectService.ajouter(collect)) {
                closeWindow();
            }
        } catch (SQLException e) {
            formError.setVisible(true);
            formError.setText("Database error: " + e.getMessage());
        }
    }

    private void clearErrors() {
        categoryError.setText("");
        titleError.setText("");
        productError.setText("");
        quantityError.setText("");
        locationError.setText("");
        dateError.setText("");
        formError.setVisible(false);
    }


    private void closeWindow() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}
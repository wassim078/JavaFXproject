package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CategorieCollectService;
import com.example.livecycle.services.CollectService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditCollectController {

    @FXML private ComboBox<CategorieCollect> categoryCombo;
    @FXML private TextField titleField;
    @FXML private TextField productField;
    @FXML private TextField quantityField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private Label errorLabel;

    private Collect currentCollect;
    private final CollectService collectService = new CollectService();
    private final CategorieCollectService categorieService = new CategorieCollectService();
    private User currentUser;



    public void initialize() {
        loadCategories();
    }
    public void setUser(User user) {
        this.currentUser = user;
    }

    public void setCollect(Collect collect) {
        this.currentCollect = collect;
        populateFields();
    }

    private void loadCategories() {
        try {
            categoryCombo.getItems().addAll(categorieService.recuperer());
        } catch (Exception e) {
            errorLabel.setText("Error loading categories: " + e.getMessage());
        }
    }

    private void populateFields() {
        if(currentCollect != null) {
            titleField.setText(currentCollect.getTitre());
            productField.setText(currentCollect.getNomProduit());
            quantityField.setText(String.valueOf(currentCollect.getQuantite()));
            locationField.setText(currentCollect.getLieu());
            datePicker.setValue(currentCollect.getDateDebut());

            // Select category
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId() == currentCollect.getCategorieCollect().getId())
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);
        }
    }

    @FXML
    private void handleSave() {
        if(validateInput()) {
            try {
                currentCollect.setTitre(titleField.getText());
                currentCollect.setNomProduit(productField.getText());
                currentCollect.setQuantite(Integer.parseInt(quantityField.getText()));
                currentCollect.setLieu(locationField.getText());
                currentCollect.setDateDebut(datePicker.getValue());
                currentCollect.setCategorieCollect(categoryCombo.getValue());

                if(currentUser != null) {
                    currentCollect.setUserId(currentUser.getId());
                }
                if(collectService.modifier(currentCollect)) {
                    getStage().close();
                }
            } catch (Exception e) {
                errorLabel.setText("Error saving changes: " + e.getMessage());
            }
        }
    }


    private Stage getStage() {
        return (Stage) titleField.getScene().getWindow();
    }





    private boolean validateInput() {
        if(titleField.getText().isEmpty() ||
                productField.getText().isEmpty() ||
                quantityField.getText().isEmpty() ||
                locationField.getText().isEmpty() ||
                datePicker.getValue() == null ||
                categoryCombo.getValue() == null) {

            errorLabel.setText("All fields are required!");
            return false;
        }

        try {
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity must be a valid number");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        getStage().close();
    }
}
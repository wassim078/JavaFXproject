package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.services.CategorieForumService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CategoryFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    private CategoryForum category;
    private CategorieForumController parentController;
    private final CategorieForumService service = new CategorieForumService();

    public CategoryFormController() throws SQLException {
    }

    public void setCategory(CategoryForum category) {
        this.category = category;
        if (category != null) {
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
        }
    }

    public void setParentController(CategorieForumController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        try {
            if (category == null) {
                category = new CategoryForum();
            }

            category.setName(nameField.getText());
            category.setDescription(descriptionField.getText());

            if (category.getId() == 0) {
                service.addCategory(category);
            } else {
                service.updateCategory(category);
            }

            parentController.refreshCategories();
            closeWindow();
        } catch (Exception e) {
            parentController.showAlert("Error", "Could not save category: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
// CategoryCollectManagementController.java
package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.services.CategorieCollectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

public class CategoryCollectManagement {

    @FXML private TableView<CategorieCollect> categoryTable;
    @FXML private TableColumn<CategorieCollect, Number> idColumn;
    @FXML private TableColumn<CategorieCollect, String> nameColumn;
    @FXML private TableColumn<CategorieCollect, String> descColumn;

    private final CategorieCollectService categoryService = new CategorieCollectService();
    private final ObservableList<CategorieCollect> categories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        loadCategories();
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
//conifgure Table
    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoryTable.setItems(categories);
    }

    private void loadCategories() {
        try {
            categories.setAll(categoryService.recuperer());
        } catch (SQLException e) {
            showError("Error loading categories", e.getMessage());
        }
    }

    @FXML
    private void handleAddCategory() {
        showCategoryDialog(null);
    }

    @FXML
    private void handleEditCategory() {
        CategorieCollect selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCategoryDialog(selected);
        }
    }

    @FXML
    private void handleDeleteCategory() {
        CategorieCollect selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (categoryService.supprimer(selected.getId())) {
                    categories.remove(selected);
                }
            } catch (SQLException e) {
                showError("Delete Error", "Cannot delete category: " + e.getMessage());
            }
        }
    }

    private void showCategoryDialog(CategorieCollect category) {
        Dialog<CategorieCollect> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "New Category" : "Edit Category");

        // Create form fields
        TextField nameField = new TextField(category != null ? category.getNom() : "");
        TextArea descField = new TextArea(category != null ? category.getDescription() : "");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                CategorieCollect newCategory = new CategorieCollect();
                if (category != null) newCategory.setId(category.getId());
                newCategory.setNom(nameField.getText());
                newCategory.setDescription(descField.getText());
                return newCategory;
            }
            return null;
        });

        Optional<CategorieCollect> result = dialog.showAndWait();
        result.ifPresent(this::saveCategory);
    }

    private void saveCategory(CategorieCollect category) {
        try {
            boolean success;
            if (category.getId() == 0) {
                success = categoryService.ajouter(category);
            } else {
                success = categoryService.modifier(category);
            }

            if (success) {
                loadCategories(); // Refresh table
            }
        } catch (SQLException e) {
            showError("Save Error", "Error saving category: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
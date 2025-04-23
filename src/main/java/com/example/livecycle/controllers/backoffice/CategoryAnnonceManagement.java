package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Category;
import com.example.livecycle.services.CategoryAnnonceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoryAnnonceManagement implements Initializable {

    @FXML private TableView<Category> categoryTable;

    private final CategoryAnnonceService categoryService = new CategoryAnnonceService();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCategories();
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCategories() {
        try {
            categories.setAll(categoryService.recuperer());
            categoryTable.setItems(categories);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void setupTable() {
        TableColumn<Category, Void> actionColumn = (TableColumn<Category, Void>) categoryTable.getColumns().get(2);

        actionColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Category, Void> call(final TableColumn<Category, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.getStyleClass().add("action-btn");
                        deleteBtn.getStyleClass().add("danger-btn");

                        editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(new HBox(5, editBtn, deleteBtn));
                        }
                    }
                };
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleCreate() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create New Category");
        dialog.setContentText("Enter category name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Category newCategory = new Category();
            newCategory.setName(name);
            try {
                if (categoryService.ajouter(newCategory)) {
                    loadCategories();
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to create category: " + e.getMessage());
            }
        });
    }
    private void handleEdit(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category");
        dialog.setContentText("Enter new name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            category.setName(newName);
            try {
                if (categoryService.modifier(category)) {
                    loadCategories();
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to update category: " + e.getMessage());
            }
        });
    }

    private void handleDelete(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Category");
        confirmation.setContentText("Are you sure you want to delete '" + category.getName() + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (categoryService.supprimer(category.getId())) {
                    loadCategories();
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete category: " + e.getMessage());
            }
        }
    }

}
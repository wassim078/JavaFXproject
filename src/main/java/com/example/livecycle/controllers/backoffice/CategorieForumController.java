package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.services.CategorieForumService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class CategorieForumController {

    @FXML private TableView<CategoryForum> categoriesTable;
    @FXML private TableColumn<CategoryForum, Integer> colId;
    @FXML private TableColumn<CategoryForum, String> colName;
    @FXML private TableColumn<CategoryForum, String> colDesc;
    @FXML private TableColumn<CategoryForum, Void> colActions;


    private final CategorieForumService categoryForumService = new CategorieForumService();
    private final ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    public CategorieForumController() throws SQLException {
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionButtons();  // Changed from setupDeleteButtons
        loadCategories();
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private void setupActionButtons() {
        colActions.setCellFactory(column -> new TableCell<CategoryForum, Void>() {
            private final HBox buttonsContainer = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Style buttons
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

                // Set button actions
                editButton.setOnAction(event -> {
                    CategoryForum category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });

                deleteButton.setOnAction(event -> {
                    CategoryForum category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });

                buttonsContainer.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsContainer);
                }
            }
        });
    }
    private void handleEditCategory(CategoryForum category) {
        showCategoryForm(category);
    }
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadCategories() {
        try {
            categories.setAll(categoryForumService.getAllCategories());
            categoriesTable.setItems(categories);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateCategory() {
        showCategoryForm(new CategoryForum());
    }

    private void showCategoryForm(CategoryForum category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/category_form.fxml"));
            Parent root = loader.load();

            CategoryFormController controller = loader.getController();
            controller.setCategory(category);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Could not load form: " + e.getMessage());
        }
    }

    public void refreshCategories() {
        loadCategories();
    }

    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        Button deleteButton = (Button) event.getSource();
        CategoryForum category = (CategoryForum) deleteButton.getUserData();

        if (category != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Category");
            confirmation.setContentText("Are you sure you want to delete '" + category.getName() + "'?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    categoryForumService.deleteCategory(category.getId());
                    refreshCategories();
                } catch (SQLException e) {
                    showAlert("Database Error", "Could not delete category: " + e.getMessage());
                }
            }
        }
    }
    private void setupDeleteButtons() {
        colActions.setCellFactory(column -> new TableCell<CategoryForum, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    CategoryForum category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }
    private void handleDeleteCategory(CategoryForum category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Category");
        confirmation.setContentText("Are you sure you want to delete '" + category.getName() + "'?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryForumService.deleteCategory(category.getId());
                refreshCategories();
            } catch (SQLException e) {
                showAlert("Database Error", "Could not delete category: " + e.getMessage());
            }
        }
    }
}
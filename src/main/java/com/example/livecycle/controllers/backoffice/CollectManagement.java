package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.entities.Collect;
import com.example.livecycle.services.CollectService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class CollectManagement {
    @FXML private TableView<Collect> collectsTable;
    @FXML private TableColumn<Collect, Void> actionsColumn;
    @FXML private TableColumn<Collect, String> titleColumn;
    @FXML private TableColumn<Collect, String> productColumn;
    @FXML private TableColumn<Collect, Number> quantityColumn;
    @FXML private TableColumn<Collect, String> locationColumn;
    @FXML private TableColumn<Collect, LocalDate> dateColumn;
    @FXML private TableColumn<Collect, String> categoryColumn;
    @FXML private TableColumn<Collect, String> emailColumn;

    private final CollectService collectService = new CollectService();
    private final ObservableList<Collect> collectsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureColumns();
        loadCollects();
        configureActionsColumn();
        setupTable();
        collectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        categoryColumn.setCellValueFactory(cellData -> {
            CategorieCollect cc = cellData.getValue().getCategorieCollect();
            return new SimpleStringProperty(cc != null ? cc.getNom() : "");
        });
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
    }

    private void loadCollects() {
        try {
            collectsData.setAll(collectService.recuperer());
            collectsTable.setItems(collectsData);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load collects: " + e.getMessage());
        }
    }

    private void setupTable() {
        collectsTable.setRowFactory(tv -> {
            TableRow<Collect> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleDelete(Collect collect) {
        if (collect != null) {
            try {
                if (collectService.supprimer(collect.getId())) {
                    collectsData.remove(collect);
                    showAlert("Success", "Collect deleted successfully!");
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete collect: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a collect to delete!");
        }
    }

    private void handleEdit(Collect collect) {
        if (collect != null) {
            showEditDialog(collect);
        } else {
            showAlert("Warning", "Please select a collect to edit!");
        }
    }

    private void showEditDialog(Collect collect) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/editCollect.fxml"));
            Parent root = loader.load();

            EditCollectController controller = loader.getController();
            controller.setCollect(collect);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Collect");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.wereChangesSaved()) {
                loadCollects();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load edit form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttons = new HBox(10);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                buttons.setAlignment(Pos.CENTER);
                buttons.setStyle("-fx-padding: 5 10;");

                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));

                buttons.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }
}
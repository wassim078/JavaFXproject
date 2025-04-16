package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CollectService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ShowMyCollectsController implements Initializable {

    @FXML private TableView<Collect> collectsTable;
    @FXML private TableColumn<Collect, String> titreColumn;
    @FXML private TableColumn<Collect, String> produitColumn;
    @FXML private TableColumn<Collect, Number> quantiteColumn;
    @FXML private TableColumn<Collect, String> lieuColumn;
    @FXML private TableColumn<Collect, String> dateDebutColumn;
    @FXML private TableColumn<Collect, String> categorieColumn;
    @FXML private TableColumn<Collect, Void> actionsColumn;

    private User currentUser;
    private final CollectService collectService = new CollectService();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadCollects();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureTableColumns();
        configureActionsColumn();
    }


    private void configureActionsColumn() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Collect, Void> call(TableColumn<Collect, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox hbox = new HBox(10, btnEdit, btnDelete);

                    {
                        // Button styling
                        btnEdit.setStyle("-fx-background-color: #a3d9b1; -fx-text-fill: #2d6b4d; -fx-font-weight: bold;");
                        btnDelete.setStyle("-fx-background-color: #ffb3b3; -fx-text-fill: #cc0000; -fx-font-weight: bold;");

                        // Button actions
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Collect collect = getTableView().getItems().get(getIndex());
                            handleEditCollect(collect);
                        });

                        btnDelete.setOnAction((ActionEvent event) -> {
                            Collect collect = getTableView().getItems().get(getIndex());
                            handleDeleteCollect(collect);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }



    private void configureTableColumns() {
        titreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        produitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomProduit()));
        quantiteColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantite()));
        lieuColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        dateDebutColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateDebut().toString()));
        categorieColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorieCollect().getNom()));
    }

    private void loadCollects() {
        try {
            if (currentUser != null) {
                collectsTable.getItems().clear();
                collectsTable.getItems().addAll(collectService.recupererParUtilisateur(currentUser.getId()));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load collects: " + e.getMessage());
        }
    }


    private void openEditWindow(Collect collect) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/edit_collect.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the collect data
            EditCollectController controller = loader.getController();
            controller.setCollect(collect);
            controller.setUser(currentUser); // If you need user context

            // Create a new stage
            Stage stage = new Stage();
            stage.setTitle("Edit Collection");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh table after editing
            loadCollects();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit window");
        }
    }



    @FXML
    private void handleEditCollect(Collect collect) {
        // Implement edit functionality
        openEditWindow(collect);
    }

    private void handleDeleteCollect(Collect collect) {
        try {
            if (collectService.supprimer(collect.getId())) {
                collectsTable.getItems().remove(collect);
                showAlert("Success", "Collect deleted successfully!");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete collect: " + e.getMessage());
        }
    }


    @FXML
    private void handleDeleteCollect(ActionEvent event) {
        Collect selected = collectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (collectService.supprimer(selected.getId())) {
                    collectsTable.getItems().remove(selected);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCreateCollect(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_collect.fxml"));
            Parent root = loader.load();

            CreateCollectController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Create New Collect");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCollects(); // Refresh table after creation

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
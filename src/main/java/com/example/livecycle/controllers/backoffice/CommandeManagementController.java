package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.services.CommandeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.sql.SQLException;

public class CommandeManagementController {

    @FXML private TableView<Commande> commandeTableView;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, String> clientNameColumn;
    @FXML private TableColumn<Commande, String> clientFamilyNameColumn;
    @FXML private TableColumn<Commande, String> methodePaiementColumn;
    @FXML private TableColumn<Commande, String> dateColumn;
    @FXML private TableColumn<Commande, String> etatCommandeColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn;

    private final CommandeService commandeService = new CommandeService();
    private ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionsColumn();
        loadCommandes();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientFamilyNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientFamilyName"));
        methodePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("methodePaiement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        etatCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("etatCommande"));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Commande, Void> call(TableColumn<Commande, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");
                    private final HBox container = new HBox(10, editBtn, deleteBtn);

                    {
                        container.setAlignment(Pos.CENTER);
                        editBtn.getStyleClass().add("modifier-btn");
                        deleteBtn.getStyleClass().add("supprimer-btn");

                        editBtn.setOnAction(event -> {
                            Commande commande = getTableView().getItems().get(getIndex());
                            handleEdit(commande);
                        });

                        deleteBtn.setOnAction(event -> {
                            Commande commande = getTableView().getItems().get(getIndex());
                            handleDelete(commande);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : container);
                    }
                };
            }
        });
    }

    private void handleEdit(Commande commande) {
        // Implement edit logic
        System.out.println("Editing commande: " + commande.getId());
    }

    private void handleDelete(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Commande");
        alert.setContentText("Are you sure you want to delete this commande?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commandeService.supprimer(commande.getId());
                    loadCommandes();
                } catch (SQLException e) {
                    showError("Delete Error", e.getMessage());
                }
            }
        });
    }

    private void loadCommandes() {
        try {
            commandeList.setAll(commandeService.recuperer());
            commandeTableView.setItems(commandeList);
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
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
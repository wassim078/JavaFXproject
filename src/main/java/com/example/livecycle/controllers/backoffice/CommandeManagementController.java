package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CommandeManagementController {

    @FXML private TableView<Commande> commandeTableView;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, String> clientNameColumn;
    @FXML private TableColumn<Commande, String> clientFamilyNameColumn;
    @FXML private TableColumn<Commande, String> methodePaiementColumn;
    @FXML private TableColumn<Commande, String> dateColumn;
    @FXML private TableColumn<Commande, String> etatCommandeColumn;
    @FXML private TableColumn<Commande, String> userEmailColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn;

    private final CommandeService commandeService = new CommandeService();
    private final UserService userService = new UserService();
    private final ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Make table fill width and allow inline editing
        commandeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        commandeTableView.setEditable(true);

        // Configure state column as editable ComboBox
        etatCommandeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                FXCollections.observableArrayList(CommandeService.getOrderStates())
        ));
        etatCommandeColumn.setOnEditCommit(evt -> {
            Commande cmd = evt.getRowValue();
            String newState = evt.getNewValue();
            try {
                commandeService.updateOrderState(cmd.getId(), newState);
                cmd.setEtatCommande(newState);
            } catch (SQLException ex) {
                showError("Erreur de mise à jour", ex.getMessage());
                cmd.setEtatCommande(evt.getOldValue());
                commandeTableView.refresh();
            }
        });

        setupTableColumns();
        setupActionsColumn();
        loadCommandes();
    }

    private void setupTableColumns() {

        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        clientFamilyNameColumn.setCellValueFactory(new PropertyValueFactory<>("clientFamilyName"));
        methodePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("methodePaiement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        etatCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("etatCommande"));

        // Fetch and display user email without altering CommandeService SQL
        userEmailColumn.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUserId();
            String email = "";
            try {
                User u = userService.getUser(userId);
                if (u != null) {
                    email = u.getEmail();
                }
            } catch (Exception e) {
                // Optionally log the error
            }
            return new SimpleStringProperty(email);
        });
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
        List<String> states = CommandeService.getOrderStates();
        ChoiceDialog<String> dlg = new ChoiceDialog<>(commande.getEtatCommande(), states);
        dlg.setTitle("Modifier l'état de commande");
        dlg.setHeaderText("Commande #" + commande.getId());
        dlg.setContentText("Nouvel état :");

        Optional<String> result = dlg.showAndWait();
        result.ifPresent(newState -> {
            if (!newState.equals(commande.getEtatCommande())) {
                try {
                    commandeService.updateOrderState(commande.getId(), newState);
                    commande.setEtatCommande(newState);
                    commandeTableView.refresh();
                } catch (SQLException ex) {
                    showError("Erreur de modification", ex.getMessage());
                }
            }
        });
    }

    private void handleDelete(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer commande");
        alert.setContentText("Êtes-vous sûr(e) de vouloir supprimer cette commande ?");

        alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(resp -> {
            try {
                commandeService.supprimer(commande.getId());
                loadCommandes();
            } catch (SQLException e) {
                showError("Erreur de suppression", e.getMessage());
            }
        });
    }

    private void loadCommandes() {
        try {
            commandeList.setAll(commandeService.recuperer());
            commandeTableView.setItems(commandeList);
        } catch (SQLException e) {
            showError("Erreur de chargement", e.getMessage());
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

package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.ReclamationDAO;
import com.example.livecycle.entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ButtonBar.ButtonData;
import com.example.livecycle.entities.User;
public class ReclamationManagementController {

    // Références FXML pour la table
    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> idColumn;
    @FXML private TableColumn<Reclamation, String> sujetColumn;
    @FXML private TableColumn<Reclamation, String> typeColumn;
    @FXML private TableColumn<Reclamation, String> etatColumn;
    @FXML private TableColumn<Reclamation, String> dateColumn;
    @FXML private TableColumn<Reclamation, Void> actionColumn;
    @FXML private TableColumn<Reclamation, String> userEmailColumn;

    // Références FXML pour la popup de modification
    @FXML private TextField editSujetField;
    @FXML private TextArea editDescriptionField;
    @FXML private ComboBox<String> editTypeComboBox;
    @FXML private ComboBox<String> editEtatComboBox;
    @FXML private TextField editFileField;
    @FXML private Hyperlink fileLink;

    private ReclamationDAO reclamationDAO;
    private ObservableList<Reclamation> reclamationList;
    private User currentUser;

    public void initialize() {
        reclamationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reclamationDAO = new ReclamationDAO();

        // Initialisation des colonnes du tableau
        configureTableColumns();

        // Configuration des ComboBox dans la popup
        editTypeComboBox.getItems().addAll("Technique", "Administrative", "Pédagogique", "Autre");
        editEtatComboBox.getItems().addAll("En Attente", "En Cours", "Résolue", "Rejetée");

        // Ajout de la colonne d'actions
        addActionButtonsToTable();

        // Chargement des données initiales
        loadReclamations();

    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sujetColumn.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateString()));
        userEmailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getEmail())
        );
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Reclamation, Void>, TableCell<Reclamation, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button editButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final HBox pane = new HBox(editButton, deleteButton);

                {
                    pane.setSpacing(5);
                    editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                    editButton.setOnAction(event -> {
                        Reclamation reclamation = getTableView().getItems().get(getIndex());
                        showEditDialog(reclamation);
                    });

                    deleteButton.setOnAction(event -> {
                        Reclamation reclamation = getTableView().getItems().get(getIndex());
                        handleDeleteReclamation(reclamation);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                    }
                }
            };
        };

        actionColumn.setCellFactory(cellFactory);
    }

    private void showEditDialog(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/edit_complaint.fxml"));
            Parent root = loader.load();

            EditComplaintController controller = loader.getController();
            controller.setReclamation(reclamation, this);

            Stage stage = new Stage();
            stage.setTitle("Edit Complaint");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Could not load edit form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void refreshTable() {
        reclamationTable.refresh();
    }


    private void updateReclamationFromDialog(Reclamation reclamation) {
        try {
            // Only update the status
            String newStatus = editEtatComboBox.getValue();
            reclamation.setEtat(newStatus);

            if (reclamationDAO.updateReclamation(reclamation)) {
                reclamationTable.refresh();
                showAlert("Succès", "État mis à jour avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de la mise à jour", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadReclamations();
    }

    private void loadReclamations() {
        try {
            List<Reclamation> reclamations;
            if (currentUser != null) {
                reclamations = reclamationDAO.getReclamationsByUser(currentUser.getId());
            } else {
                reclamations = reclamationDAO.getAllReclamations();
            }

            reclamationList = FXCollections.observableArrayList(reclamations);
            reclamationTable.setItems(reclamationList);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteReclamation(Reclamation reclamation) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer la réclamation");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (reclamationDAO.deleteReclamation(reclamation.getId())) {
                    reclamationList.remove(reclamation);
                    showAlert("Succès", "Réclamation supprimée avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Échec de la suppression", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
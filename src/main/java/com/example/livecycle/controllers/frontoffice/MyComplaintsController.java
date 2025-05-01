package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Reclamation;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.ReclamationDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyComplaintsController implements Initializable {

    @FXML private ListView<Reclamation> complaintsList;
    @FXML private TableView<Reclamation> complaintsTable;
    @FXML private TableColumn<Reclamation, String> subjectCol;
    @FXML private TableColumn<Reclamation, String> descriptionCol;
    @FXML private TableColumn<Reclamation, String> typeCol;
    @FXML private TableColumn<Reclamation, String> statusCol;
    @FXML private TableColumn<Reclamation, String> dateCol;
    @FXML private TableColumn<Reclamation, Void> actionCol;
    private User currentUser;
    private final ReclamationDAO reclamationDAO = new ReclamationDAO();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadComplaints();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
    }
    private void configureTableColumns() {
        // Set up cell value factories
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateString"));

        // Create common cell factory for left-aligned text
        Callback<TableColumn<Reclamation, String>, TableCell<Reclamation, String>> textCellFactory =
                column -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 10px;");
                        }
                    }
                };

        // Apply cell factories to columns
        subjectCol.setCellFactory(textCellFactory);
        descriptionCol.setCellFactory(textCellFactory);
        typeCol.setCellFactory(textCellFactory);
        dateCol.setCellFactory(textCellFactory);

        // Custom cell factory for status column
        statusCol.setCellFactory(column -> new TableCell<>() {
            private final Label statusLabel = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    statusLabel.setText(status);
                    statusLabel.getStyleClass().addAll("status-badge", getStatusStyle(status));
                    setGraphic(statusLabel);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Configure table properties
        complaintsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        complaintsTable.setFixedCellSize(60);  // Set row height

        // Add spacing between rows
        complaintsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Reclamation item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-insets: 0 0 10px 0, 0 0 3px 0;");
            }
        });
        configureActionColumn();
    }

    private void configureActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                // Button styling and action
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    deleteComplaint(reclamation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }
    private void deleteComplaint(Reclamation reclamation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Complaint");
        confirmation.setContentText("Are you sure you want to delete this complaint?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = reclamationDAO.deleteReclamation(reclamation.getId());
            if (success) {
                complaintsTable.getItems().remove(reclamation);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Complaint deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete complaint!");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void configureListView() {
        complaintsList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Reclamation> call(ListView<Reclamation> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Reclamation reclamation, boolean empty) {
                        super.updateItem(reclamation, empty);
                        if (empty || reclamation == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(createComplaintCard(reclamation));
                        }
                    }
                };
            }
        });
    }

    private VBox createComplaintCard(Reclamation reclamation) {
        VBox card = new VBox();
        card.getStyleClass().add("complaint-card");

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("complaint-header");

        Label title = new Label(reclamation.getSujet());
        title.getStyleClass().add("complaint-title");

        Label status = new Label(reclamation.getEtat());
        status.getStyleClass().addAll("status-badge", getStatusStyle(reclamation.getEtat()));

        header.getChildren().addAll(title, status);

        // Details
        VBox details = new VBox();
        details.getStyleClass().add("complaint-details");

        addDetailItem(details, "Description:", reclamation.getDescription());
        addDetailItem(details, "Type:", reclamation.getType());
        addDetailItem(details, "Date:", reclamation.getDateString());

        card.getChildren().addAll(header, details);
        return card;
    }

    private void addDetailItem(VBox container, String label, String value) {
        HBox item = new HBox();
        item.getStyleClass().add("detail-item");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("detail-label");

        Label val = new Label(value);
        val.getStyleClass().add("detail-value");

        item.getChildren().addAll(lbl, val);
        container.getChildren().add(item);
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "en cours" -> "status-en-cours";
            case "résolue" -> "status-resolue";
            case "rejetée" -> "status-rejetée";
            default -> "status-en-attente";
        };
    }

    private void loadComplaints() {
        if (currentUser != null) {
            System.out.println("DEBUG: Loading complaints for user ID: " + currentUser.getId());

            List<Reclamation> complaints = reclamationDAO.getReclamationsByUser(currentUser.getId());
            System.out.println("DEBUG: Found " + complaints.size() + " complaints");

            complaintsTable.setItems(FXCollections.observableArrayList(complaints));

            // Debug output
            complaints.forEach(c -> System.out.println(
                    "Complaint ID: " + c.getId() +
                            " | Subject: " + c.getSujet() +
                            " | Status: " + c.getEtat()
            ));
        } else {
            System.out.println("DEBUG: No current user set");
        }
    }
}
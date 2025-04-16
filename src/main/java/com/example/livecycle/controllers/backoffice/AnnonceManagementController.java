package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.controllers.backoffice.EditAnnonceController;
import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AnnonceManagementController {

    @FXML private TableView<Annonce> annoncesTable;
    @FXML private TableColumn<Annonce, Integer> idColumn;
    @FXML private TableColumn<Annonce, String> titleColumn;
    @FXML private TableColumn<Annonce, String> descriptionColumn;
    @FXML private TableColumn<Annonce, Category> categoryColumn;
    @FXML private TableColumn<Annonce, Double> weightColumn;
    @FXML private TableColumn<Annonce, Double> priceColumn;
    @FXML private TableColumn<Annonce, Integer> quantityColumn;
    @FXML private TableColumn<Annonce, String> userEmailColumn;
    @FXML private TableColumn<Annonce, String> imageColumn;
    @FXML private TableColumn<Annonce, Void> actionsColumn; // Added missing column declaration

    private final AnnonceService annonceService = new AnnonceService();
    private ObservableList<Annonce> annoncesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAnnouncements();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorieAnnonce"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("poids"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));

        // Category column formatting
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // Image column
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                        } else {
                            // Load default image from resources
                            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-annonce.png"));
                            imageView.setImage(defaultImage);
                        }
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(80);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        // Log error and set default image
                        System.err.println("Error loading image: " + e.getMessage());
                        imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-annonce.png")));
                        setGraphic(imageView);
                    }
                }
            }
        });

        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

                editBtn.setOnAction(e -> {
                    Annonce annonce = getTableView().getItems().get(getIndex());
                    handleEdit(annonce);
                });

                deleteBtn.setOnAction(e -> {
                    Annonce annonce = getTableView().getItems().get(getIndex());
                    handleDelete(annonce);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadAnnouncements() {
        try {
            annoncesData.setAll(annonceService.getAllWithUserEmail());
            annoncesTable.setItems(annoncesData);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load announcements: " + e.getMessage());
        }
    }

    private void handleDelete(Annonce annonce) {
        if (annonce != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setContentText("Delete this announcement?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        if (annonceService.supprimer(annonce.getId())) {
                            loadAnnouncements();
                        }
                    } catch (SQLException e) {
                        showAlert("Delete Error", "Failed to delete announcement: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void handleEdit(Annonce annonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/edit_annonce.fxml"));
            Parent root = loader.load();

            EditAnnonceController controller = loader.getController();
            controller.setAnnonce(annonce, this::loadAnnouncements);

            Stage stage = new Stage();
            stage.setTitle("Edit Announcement");
            stage.setScene(new Scene(root));
            stage.show();
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
}
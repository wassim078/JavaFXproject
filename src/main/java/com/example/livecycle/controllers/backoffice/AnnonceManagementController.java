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
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AnnonceManagementController {

    @FXML private TableView<Annonce> annoncesTable;
    @FXML private TableColumn<Annonce, String> titleColumn;
    @FXML private TableColumn<Annonce, String> descriptionColumn;
    @FXML private TableColumn<Annonce, Category> categoryColumn;
    @FXML private TableColumn<Annonce, Double> weightColumn;
    @FXML private TableColumn<Annonce, Double> priceColumn;
    @FXML private TableColumn<Annonce, Integer> quantityColumn;
    @FXML private TableColumn<Annonce, String> userEmailColumn;
    @FXML private TableColumn<Annonce, String> imageColumn;
    @FXML private TableColumn<Annonce, Void> actionsColumn;

    private final AnnonceService annonceService = new AnnonceService();
    private final UserService userService = new UserService();
    private ObservableList<Annonce> annoncesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAnnouncements();
        
        // Apply modern styling to the table
        annoncesTable.getStyleClass().add("modern-table");
    }

    private void setupTableColumns() {
        // Configure cell value factories
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
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.getName());
                    label.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 5 10; -fx-background-radius: 4; -fx-text-fill: #2e7d32;");
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Image column with improved styling
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(60);
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("image-view");
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(imagePath);
                        Image image;
                        if (file.exists()) {
                            image = new Image(file.toURI().toString(), 80, 60, true, true);
                        } else {
                            image = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-annonce.png"),
                                    80, 60, true, true);
                        }
                        imageView.setImage(image);
                        setGraphic(imageView);
                        getStyleClass().add("image-cell");
                        setAlignment(Pos.CENTER);
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                        try {
                            Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-annonce.png"),
                                    80, 60, true, true);
                            imageView.setImage(defaultImage);
                            setGraphic(imageView);
                        } catch (Exception ex) {
                            setGraphic(null);
                        }
                    }
                }
            }
        });

        // Actions column with styled buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("button", "edit-button");
                deleteBtn.getStyleClass().addAll("button", "delete-button");
                buttons.setAlignment(Pos.CENTER);

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
                setAlignment(Pos.CENTER);
            }
        });

        // Add hover effect to rows
        annoncesTable.setRowFactory(tv -> {
            TableRow<Annonce> row = new TableRow<>();
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f8f9fa;");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("");
                }
            });
            return row;
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
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Announcement");
            confirmation.setContentText("Are you sure you want to delete this announcement?");
            confirmation.getDialogPane().setStyle("-fx-background-color: white;");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        if (annonceService.supprimer(annonce.getId())) {
                            // Send notification to the user
                            String notificationMessage = "Your announcement '" + annonce.getTitre() + "' has been deleted by an administrator.";
                            userService.addNotification(annonce.getUserId(), notificationMessage);
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
            Scene scene = new Scene(root);
            stage.setScene(scene);
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
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        alert.showAndWait();
    }
}
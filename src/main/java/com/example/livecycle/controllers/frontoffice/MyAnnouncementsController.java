package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MyAnnouncementsController {
    @FXML private GridPane annoncesGrid;
    private User currentUser;
    private final AnnonceService annonceService = new AnnonceService();
    private Runnable refreshCallback;
    @FXML private Button createBtn;
    private UserDashboardController dashboardController;
    @FXML private BorderPane root; // Reference to the root BorderPane
    @FXML private HBox topSection; // Reference to the top HBox
    @FXML private ScrollPane mainScrollPane; // Reference to the center ScrollPane

    // Add this method

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserAnnouncements();
    }

    public void setDashboardController(UserDashboardController controller) {
        this.dashboardController = controller;
    }




    private void loadUserAnnouncements() {
        try {
            List<Annonce> annonces = annonceService.getByUserId(currentUser.getId());
            annoncesGrid.getChildren().clear();

            int row = 0;
            for (Annonce annonce : annonces) {
                addAnnouncementCard(annonce, row);
                row++;
            }
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }

    private void addAnnouncementCard(Annonce annonce, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("annonce-card");
        HBox imageContainer = new HBox();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setAlignment(Pos.CENTER);
        // Image Section
        ImageView imageView = new ImageView();
        try {
            if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
                File file = new File(annonce.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-annonce.png")));
        }
        imageView.setFitWidth(320);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("annonce-image");

        // Details Section
        VBox details = new VBox();
        details.getStyleClass().add("detail-section");

        Text title = new Text(annonce.getTitre());
        title.getStyleClass().add("title-text");

        Label category = new Label(annonce.getCategorieAnnonce().getName());
        category.getStyleClass().add("category-tag");

        Text price = new Text(String.format("%.2f TND", annonce.getPrix()));
        price.getStyleClass().add("price-text");

        Label quantity = new Label("Available: " + annonce.getQuantite());
        quantity.getStyleClass().add("detail-label");

        // Action Buttons
        HBox buttons = new HBox();
        buttons.getStyleClass().add("action-buttons");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        editBtn.getStyleClass().add("edit-btn");
        deleteBtn.getStyleClass().add("delete-btn");

        editBtn.setOnAction(e -> handleEditAnnouncement(annonce));
        deleteBtn.setOnAction(e -> handleDeleteAnnouncement(annonce));

        // Build Layout
        details.getChildren().addAll(title, category, price, quantity);
        buttons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageView, details, buttons);

        // Calculate position
        int column = index % 3;
        int row = index / 3;
        annoncesGrid.add(card, column, row);
    }

    @FXML
    private void handleCreateAnnonce() {
        try {
            // Load the form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_annonce.fxml"));
            Parent form = loader.load();

            // Get the form's controller
            CreateAnnonceController formController = loader.getController();
            formController.setUser(currentUser);

            formController.setRefreshCallback(this::loadUserAnnouncements);

            // Define what happens when the form is closed/cancelled
            formController.setBackCallback(() -> {
                root.setTop(topSection); // Restore the original top
                root.setCenter(mainScrollPane); // Restore the original center
            });

            // Replace the entire view with the form
            root.setTop(null); // Remove the top section
            root.setCenter(form); // Set form as the new center

        } catch (IOException e) {
            showError("Error loading form: " + e.getMessage());
        }
    }

    private void handleEditAnnouncement(Annonce annonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/edit_annonce.fxml"));
            VBox form = loader.load();

            EditAnnonceController controller = loader.getController();
            controller.setAnnonce(annonce);
            controller.setRefreshCallback(() -> {
                loadUserAnnouncements(); // Force refresh
            });


            annoncesGrid.getChildren().clear();
            annoncesGrid.add(form, 0, 0);

        } catch (IOException e) {
            showError("Error loading edit form: " + e.getMessage());
        }
    }

    private void handleDeleteAnnouncement(Annonce annonce) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Announcement");
        confirmation.setContentText("Are you sure you want to delete this announcement?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (annonceService.supprimer(annonce.getId())) {
                    loadUserAnnouncements();
                    showSuccess("Announcement deleted successfully!");
                }
            } catch (SQLException e) {
                showError("Error deleting announcement: " + e.getMessage());
            }
        }
    }
    public void cleanup() {
        if (dashboardController != null) {
            dashboardController.annonceBtn.setVisible(true);
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }

    private void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).show();
    }
}
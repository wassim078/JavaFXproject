package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserAnnouncements();
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

    private void addAnnouncementCard(Annonce annonce, int row) {
        VBox card = new VBox(15);
        card.getStyleClass().add("annonce-card");

        // Image
        ImageView imageView = new ImageView();
        try {
            if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
                File file = new File(annonce.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-annonce.png")));
        }
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Details
        VBox details = new VBox(10);
        details.getChildren().addAll(
                new Text("Title: " + annonce.getTitre()),
                new Text("Category: " + annonce.getCategorieAnnonce().getName()),
                new Text("Price: " + annonce.getPrix() + " TND"),
                new Text("Quantity: " + annonce.getQuantite())
        );

        // Action Buttons
        HBox buttons = new HBox(10);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-btn");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");

        editBtn.setOnAction(e -> handleEditAnnouncement(annonce));
        deleteBtn.setOnAction(e -> handleDeleteAnnouncement(annonce));

        buttons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageView, details, buttons);
        annoncesGrid.add(card, 0, row);
    }

    @FXML
    private void handleCreateAnnonce() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_annonce.fxml"));
            GridPane form = loader.load();

            CreateAnnonceController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setRefreshCallback(this::loadUserAnnouncements);

            // Replace grid content with form
            annoncesGrid.getChildren().clear();
            annoncesGrid.add(form, 0, 0);

        } catch (IOException e) {
            showError("Error loading creation form: " + e.getMessage());
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

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }

    private void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).show();
    }
}
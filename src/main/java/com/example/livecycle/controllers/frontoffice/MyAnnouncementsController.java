package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyAnnouncementsController implements Initializable {
    @FXML private GridPane annoncesGrid;
    private User currentUser;
    private final AnnonceService annonceService = new AnnonceService();
    private final UserService userService = new UserService();
    private Runnable refreshCallback;
    @FXML private Button createBtn;
    private UserDashboardController dashboardController;
    @FXML private BorderPane root; // Reference to the root BorderPane
    @FXML private HBox topSection; // Reference to the top HBox
    @FXML private ScrollPane mainScrollPane; // Reference to the center ScrollPane
    @FXML private VBox notificationsBox;
    @FXML private Button notificationBtn;
    private List<String> allNotifications = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize notifications when the user is set
    }

    private void setupNotifications() {
        if (currentUser == null) return; // Don't setup notifications if no user is set
        
        try {
            List<String> notifications = userService.getNotifications(currentUser.getId());
            notificationsBox.getChildren().clear();
            
            for (String notification : notifications) {
                HBox notificationBox = new HBox();
                notificationBox.getStyleClass().add("notification-box");
                notificationBox.setSpacing(10);
                notificationBox.setPadding(new Insets(10));
                
                Label messageLabel = new Label(notification);
                messageLabel.getStyleClass().add("notification-message");
                
                Button clearBtn = new Button("Clear");
                clearBtn.getStyleClass().add("clear-notification-btn");
                clearBtn.setOnAction(e -> {
                    try {
                        // Remove only this specific notification
                        userService.removeNotification(currentUser.getId(), notification);
                        // Refresh the notifications display
                        setupNotifications();
                    } catch (SQLException ex) {
                        showError("Error clearing notification: " + ex.getMessage());
                    }
                });
                
                notificationBox.getChildren().addAll(messageLabel, clearBtn);
                notificationsBox.getChildren().add(notificationBox);
            }
        } catch (SQLException e) {
            showError("Error loading notifications: " + e.getMessage());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserAnnouncements();
        setupNotifications();
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

    @FXML
    private void showNotificationHistory() {
        try {
            allNotifications = userService.getNotifications(currentUser.getId());
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Notification History");
            dialog.setHeaderText("All Notifications");
            
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStyleClass().add("notification-history");
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            
            VBox content = new VBox(10);
            content.getStyleClass().add("list");
            
            // Add a clear all button
            Button clearAllBtn = new Button("Clear All Notifications");
            clearAllBtn.getStyleClass().add("clear-all-btn");
            clearAllBtn.setOnAction(e -> {
                try {
                    userService.clearNotifications(currentUser.getId());
                    allNotifications.clear();
                    setupNotifications();
                    dialog.close();
                } catch (SQLException ex) {
                    showError("Error clearing notifications: " + ex.getMessage());
                }
            });
            
            // Add notifications in reverse chronological order
            for (int i = allNotifications.size() - 1; i >= 0; i--) {
                String notification = allNotifications.get(i);
                VBox notificationItem = new VBox(5);
                notificationItem.getStyleClass().add("list-item");
                
                TextFlow message = new TextFlow();
                Text text = new Text(notification);
                message.getChildren().add(text);
                
                // Add timestamp
                LocalDateTime now = LocalDateTime.now();
                String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Text timeText = new Text("\n" + timestamp);
                timeText.getStyleClass().add("timestamp");
                message.getChildren().add(timeText);
                
                notificationItem.getChildren().add(message);
                content.getChildren().add(notificationItem);
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            
            VBox mainContent = new VBox(10, clearAllBtn, scrollPane);
            dialogPane.setContent(mainContent);
            
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error loading notification history: " + e.getMessage());
        }
    }
}
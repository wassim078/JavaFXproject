package com.example.livecycle.controllers;

import com.example.livecycle.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;  // This is the crucial import
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.io.InputStream;


public class AdminDashboardController {
    @FXML private VBox sidebarMenu;
    @FXML private StackPane contentArea;
    @FXML private ImageView userPhoto;
    @FXML private Label userName;
    @FXML private VBox profileMenu;
    private static final String UPLOAD_DIR = "uploads/";
    private User currentUser;
    private Button activeButton = null;



    private void setActiveButton(Button clickedButton) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }

        activeButton = clickedButton;
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    public void initData(User user) {
        this.currentUser = user;
        try {
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                File imageFile = new File(UPLOAD_DIR + user.getImage());
                if (imageFile.exists()) {
                    userPhoto.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    loadDefaultAvatar();
                }
            } else {
                loadDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
            loadDefaultAvatar();
        }
    }


    private void loadDefaultAvatar() {
        try (InputStream defaultStream = getClass().getResourceAsStream("/com/example/livecycle/default-avatar.png")) {
            userPhoto.setImage(new Image(defaultStream));
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void hideProfileMenu() {
        profileMenu.setVisible(false);
        profileMenu.setMouseTransparent(true);
    }

    @FXML
    private void toggleProfileMenu() {
        // Force the menu to the top of the stacking order
        profileMenu.toFront();
        // Toggle visibility
        profileMenu.setVisible(!profileMenu.isVisible());
        // Ensure it can receive mouse clicks when visible
        profileMenu.setMouseTransparent(!profileMenu.isVisible());
    }


    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/admin_profile_view.fxml"));
            Parent profileView = loader.load();

            ProfileViewController controller = loader.getController();
            controller.initializeUserData(currentUser);

            Stage profileStage = new Stage();
            profileStage.setTitle("Admin Profile");
            profileStage.setScene(new Scene(profileView));
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/logo.png")));
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile view");
        }

        profileMenu.setVisible(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleLogout() {
        profileMenu.setVisible(false);
        // Add logout logic
        System.out.println("Logout clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userPhoto.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }








    // Menu Action Handlers
    @FXML private void showDashboard(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/admin_default.fxml");
        hideProfileMenu();

    }

    @FXML private void showUserManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/user_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showAnnonceManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/annonce_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showCollectManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/collect_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showReclamationManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/reclamation_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showCategoryManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/category_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showCommandeManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/commande_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showForumManagement(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/commande_management.fxml");
        hideProfileMenu();
    }


}
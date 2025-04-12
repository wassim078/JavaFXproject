package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.controllers.auth.LoginController;
import com.example.livecycle.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserDashboardController {
    @FXML private Button dashboardBtn;
    @FXML private Button collectBtn;
    @FXML private Button annonceBtn;
    @FXML private Button reclamationBtn;
    @FXML private Button commandBtn;
    @FXML private Button forumBtn;

    @FXML
    private ImageView userPhoto;

    private User currentUser;
    private static final String UPLOADS_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String DEFAULT_AVATAR = "/com/example/livecycle/images/default-avatar.png";
    private ContextMenu profileMenu;
    private MenuItem editProfileItem;
    private MenuItem notificationsItem;

    public void initialize() {
        // Set initial active state
        setActiveButton(dashboardBtn);
        loadInitialView();
        setupAvatarClipping();
        createProfileMenu();
    }


    @FXML
    private void handleAvatarClick(javafx.scene.input.MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            profileMenu.show(userPhoto, event.getScreenX(), event.getScreenY());
        }
    }




    private void setupAvatarClipping() {
        Circle clip = new Circle();
        clip.radiusProperty().bind(userPhoto.fitWidthProperty().divide(2));
        clip.centerXProperty().bind(userPhoto.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(userPhoto.fitHeightProperty().divide(2));
        userPhoto.setClip(clip);
    }


    private void createProfileMenu() {
        profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("profile-menu");

        // Edit Profile Item
        editProfileItem = new MenuItem("Edit Profile");
        editProfileItem.getStyleClass().add("profile-menu-item");
        editProfileItem.setOnAction(e -> showEditProfile());

        // Notifications Item
        notificationsItem = new MenuItem("Notifications");
        notificationsItem.getStyleClass().add("profile-menu-item");
        notificationsItem.setOnAction(e -> showNotifications());

        // Separator
        SeparatorMenuItem separator = new SeparatorMenuItem();
        separator.getStyleClass().add("menu-separator");

        profileMenu.getItems().addAll(editProfileItem, separator, notificationsItem);
    }

    private void showEditProfile() {
        showEditUser();
        System.out.println("Edit Profile clicked");
    }
    private void showNotifications() {
        // Implement notifications logic
        System.out.println("Notifications clicked");
    }

    private void loadInitialView() {
        setActiveButton(dashboardBtn);
        loadView("/com/example/livecycle/frontoffice/dashboard.fxml");
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        List<Button> navButtons = Arrays.asList(
                dashboardBtn, collectBtn, annonceBtn,
                reclamationBtn, commandBtn, forumBtn
        );

        navButtons.forEach(btn -> {
            btn.getStyleClass().remove("active");
            btn.setStyle(""); // Clear inline styles if any
        });

        // Add active class to clicked button
        if (activeButton != null) {
            activeButton.getStyleClass().add("active");
        }
    }
    private void deleteActiveButton() {
        // Remove active class from all buttons
        List<Button> navButtons = Arrays.asList(
                dashboardBtn, collectBtn, annonceBtn,
                reclamationBtn, commandBtn, forumBtn
        );

        navButtons.forEach(btn -> {
            btn.getStyleClass().remove("active");
            btn.setStyle(""); // Clear inline styles if any
        });

    }



    public void initData(User user) {
        this.currentUser = user;
        loadUserAvatar();
    }

    private void loadUserAvatar() {
        try {
            String imagePath = currentUser.getEffectiveImagePath();

            // Handle remote images
            if (imagePath.startsWith("http")) {
                Image image = new Image(imagePath, true);
                image.errorProperty().addListener((obs, wasError, isError) -> {
                    if (isError) {
                        System.err.println("Error loading remote image: " + imagePath);
                        setDefaultAvatar();
                    }
                });
                userPhoto.setImage(image);
                return;
            }

            // Handle local files
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                userPhoto.setImage(new Image(imageFile.toURI().toString()));
            } else {
                setDefaultAvatar();
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar: " + e.getMessage());
            setDefaultAvatar();
        }
    }




    @FXML
    private StackPane contentArea; // Référence au StackPane du FXML

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view); // Remplace le contenu
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes de navigation
    public void showDashboard(ActionEvent actionEvent) {
        setActiveButton(dashboardBtn);
        loadView("/com/example/livecycle/frontoffice/dashboard.fxml");
    }

    public void showCollectManagement(ActionEvent actionEvent) {
        setActiveButton(collectBtn);
        loadView("/com/example/livecycle/backoffice/collect_management.fxml");
    }

    public void showAnnonceManagement(ActionEvent actionEvent) {
        setActiveButton(annonceBtn);
        loadView("/com/example/livecycle/backoffice/annonce_management.fxml");
    }

    public void showReclamationManagement(ActionEvent actionEvent) {
        setActiveButton(reclamationBtn);
        loadView("/com/example/livecycle/backoffice/reclamation_management.fxml");
    }
    public void showCommandManagement(ActionEvent actionEvent) {
        setActiveButton(commandBtn);
        loadView("/com/example/livecycle/backoffice/commande_management.fxml");
    }
    public void showForumManagement(ActionEvent actionEvent) {
        setActiveButton(forumBtn);
        loadView("/com/example/livecycle/backoffice/forum_management.fxml");
    }
    public void showEditUser() {
        deleteActiveButton();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/edit_profile.fxml"));
            Parent view = loader.load();
            EditProfileController controller = loader.getController();
            controller.initData(currentUser,this);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showLoadError("Edit Profile", e);
        }
    }
    private void showLoadError(String screenName, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Loading Error");
        alert.setHeaderText("Error loading " + screenName);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public void handleLogout(ActionEvent actionEvent) {

            LoginController.stopCallbackServer();
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/login.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) userPhoto.getScene().getWindow();

            // Set the new scene
            stage.setScene(new Scene(root));

            // Optional: Clear any user data or session information
            this.currentUser = null;

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Logout Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load login screen");
            alert.showAndWait();
        }

    }




    private void setDefaultAvatar() {
        userPhoto.setImage(new Image(
                getClass().getResourceAsStream(DEFAULT_AVATAR)
        ));
    }


    public void refreshUserAvatar() {
        loadUserAvatar();
    }

}



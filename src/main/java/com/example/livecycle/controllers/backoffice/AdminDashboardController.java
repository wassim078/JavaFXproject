package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.controllers.RefreshableController;
import com.example.livecycle.entities.User;
import com.example.livecycle.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;  // This is the crucial import
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class AdminDashboardController implements RefreshableController {
    @FXML private VBox sidebarMenu;
    @FXML private StackPane contentArea;
    @FXML private ImageView userPhoto;
    @FXML private Label userName;
    @FXML private VBox profileMenu;
    @FXML private VBox categorySubmenu;
    @FXML private Button categoryManagementButton;
    @FXML private ImageView categoryArrow;


    private static final String UPLOAD_DIR = "uploads/";
    private User currentUser;
    private Button activeButton = null;



    public void initialize() {
        // Existing initialization code
        configureSessionPersistence();
        userPhoto.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                configureSessionPersistence();
            }
        });
    }

    private void configureSessionPersistence() {
        try {
            Stage stage = (Stage) userPhoto.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                // Maintain existing cleanup code
                SessionManager.saveSession(currentUser.getId());
                // Add any other cleanup you need here
            });
        } catch (Exception e) {
            System.err.println("Error configuring session persistence: " + e.getMessage());
        }
    }


    private void setActiveButton(Button clickedButton) {
        clearActiveStyle(sidebarMenu); // Remove 'active' from all buttons
        clickedButton.getStyleClass().add("active"); // Activate the clicked button
        activeButton = clickedButton;
    }



    private void clearActiveStyle(VBox parent) {
        parent.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            } else if (node instanceof VBox) {
                // Special handling for category submenu
                if (node == categorySubmenu) {
                    ((VBox) node).getChildren().forEach(subNode -> {
                        if (subNode instanceof Button) {
                            subNode.getStyleClass().remove("active");
                        }
                    });
                } else {
                    clearActiveStyle((VBox) node);
                }
            }
        });
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

        Image rightArrow = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/arrow-right.png"));
        categoryArrow.setImage(rightArrow);
    }


    private void loadDefaultAvatar() {
        try (InputStream defaultStream = getClass().getResourceAsStream("/com/example/livecycle/images/default-avatar.png")) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/admin_profile_view.fxml"));
            Parent profileView = loader.load();

            ProfileViewController controller = loader.getController();
            controller.initializeUserData(currentUser);

            Stage profileStage = new Stage();
            profileStage.setTitle("Admin Profile");
            profileStage.setScene(new Scene(profileView));
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/logo.png")));
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
        SessionManager.clearSession();
        profileMenu.setVisible(false);
        // Add logout logic
        System.out.println("Logout clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userPhoto.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    @FXML private void showMainDashboard(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/admin_dashboard.fxml");
        hideProfileMenu();

    }


    // Menu Action Handlers
    @FXML private void showDashboard(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        closeAllSubmenus();
        loadContent("/com/example/livecycle/backoffice/admin_default.fxml");
        hideProfileMenu();

    }

    @FXML private void showUserManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/user_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showAnnonceManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/annonce_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showCollectManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/collect_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showReclamationManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/reclamation_management.fxml");
        hideProfileMenu();
    }

    @FXML private void showCommandeManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/commande_management.fxml");
        hideProfileMenu();
    }
    @FXML private void showForumManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/forum_management.fxml");
        hideProfileMenu();
    }

    public void showAdminForumManagement(ActionEvent event) {
        closeAllSubmenus();
        setActiveButton((Button) event.getSource());
        loadContent("/com/example/livecycle/backoffice/admin_forum_management.fxml");
        hideProfileMenu();
    }

    @FXML
    private void toggleCategorySubmenu(ActionEvent event) {
        boolean isVisible = categorySubmenu.isVisible();
        categorySubmenu.setVisible(!isVisible);
        categorySubmenu.setManaged(!isVisible);
        Button button = (Button) event.getSource();


        if (isVisible) {
            Image rightArrow = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/arrow-right.png"));
            categoryArrow.setImage(rightArrow);
        } else {
            Image downArrow = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/arrow-down.png"));
            categoryArrow.setImage(downArrow);
        }


        setActiveButton(button); // Use centralized logic to handle active state
    }




    // Submenu button handlers
    @FXML
    private void showCategoryForum(ActionEvent event) {
        setActiveButton((Button) event.getSource()); // Highlight clicked sub-button
        loadContent("/com/example/livecycle/backoffice/category_forum_management.fxml");
        hideProfileMenu();
    }

    @FXML
    private void showCategoryCollect(ActionEvent event) {

        setActiveButton((Button) event.getSource()); // Highlight clicked sub-button
        loadContent("/com/example/livecycle/backoffice/category_collect_management.fxml");
        hideProfileMenu();
    }

    @FXML
    private void showCategoryAnnonce(ActionEvent event) {
        setActiveButton((Button) event.getSource()); // Highlight clicked sub-button
        loadContent("/com/example/livecycle/backoffice/category_annonce_management.fxml");
        hideProfileMenu();
    }

    // Add this method to close submenus when other main buttons are clicked
    private void closeAllSubmenus() {
        categorySubmenu.setVisible(false);
        categorySubmenu.setManaged(false);
        categoryManagementButton.getStyleClass().remove("active");

        // Deactivate submenu buttons
        categorySubmenu.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            }
        });


        Image rightArrow = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/arrow-right.png"));
        categoryArrow.setImage(rightArrow);

    }

    // Update existing show methods to close submenus

    @Override
    public void refreshVerificationStatus() {
        // Implement refresh logic if needed
        // Can leave empty if not used for admin
    }

}
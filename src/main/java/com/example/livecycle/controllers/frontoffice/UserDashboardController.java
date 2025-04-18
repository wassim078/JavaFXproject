package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.controllers.auth.LoginController;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UserDashboardController {
    @FXML private Button dashboardBtn;
    @FXML private MenuButton collectBtn;
    @FXML private MenuButton annonceBtn;
    @FXML private MenuButton reclamationBtn;
    @FXML private Button commandBtn;
    @FXML private Button forumBtn;
    @FXML private StackPane cartBadge;
    @FXML private Label cartCountLabel;
    private int            cartCount = 0;

    private final CommandeService commandeService = new CommandeService();


    @FXML
    private ImageView userPhoto;

    private User currentUser;
    private static final String UPLOADS_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String DEFAULT_AVATAR = "/com/example/livecycle/images/default-avatar.png";
    private ContextMenu profileMenu;
    private MenuItem editProfileItem;
    private MenuItem notificationsItem;
    private final UserService userService = new UserService();


    private void configureMenuBasedOnRoles() {
        if (currentUser != null) {
            boolean isEnterprise = hasRole("ROLE_ENTREPRISE");

            // Hide "My Announcements" menu item
            annonceBtn.getItems().get(0).setVisible(!isEnterprise); // Index 0 is "My Announcements"

            // Also hide other UI elements if needed
        }
    }
    public void initialize() {
        // Set initial active state
        setActiveButton(dashboardBtn);
        loadInitialView();
        setupAvatarClipping();
        createProfileMenu();
        configureMenuBasedOnRoles();
        cartBadge.setVisible(false);
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
        refreshCurrentUser();
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

    private void setActiveButton(Node activeControl) {
        // Clear existing active states
        List<Node> navControls = Arrays.asList(
                dashboardBtn, collectBtn, annonceBtn,
                reclamationBtn, commandBtn, forumBtn
        );

        navControls.forEach(control -> {
            if (control instanceof ButtonBase) { // Handles both Button and MenuButton
                control.getStyleClass().remove("active");
            }
        });

        // Set new active state
        if (activeControl != null && activeControl instanceof ButtonBase) {
            activeControl.getStyleClass().add("active");
        }
    }
    private void deleteActiveButton() {
        List<Node> navControls = Arrays.asList(
                dashboardBtn, collectBtn, annonceBtn,
                reclamationBtn, commandBtn, forumBtn
        );

        navControls.forEach(control -> {
            if (control instanceof ButtonBase) {
                control.getStyleClass().remove("active");
                ((ButtonBase) control).setStyle("");
            }
        });
    }



    public void initData(User user) {
        this.currentUser = user;
        loadUserAvatar();
        loadCartCount();
    }

    void loadCartCount() {
        try {
            int total = commandeService.getTotalQuantityByUser(currentUser.getId());
            updateCartBadge(total);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCartBadge(int count) {
        cartCount = count;
        cartCountLabel.setText(String.valueOf(count));
        cartBadge.setVisible(count > 0);
    }

    public void incrementCartCount() {
        updateCartBadge(cartCount + 1);
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

            // Add null check for contentArea
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            } else {
                System.err.println("Error: contentArea is not initialized!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showLoadError("View Loading", e);
        }
    }








    // Méthodes de navigation
    public void showDashboard(ActionEvent actionEvent) {
        refreshCurrentUser();
        setActiveButton(dashboardBtn);
        loadView("/com/example/livecycle/frontoffice/dashboard.fxml");
    }




    public void showMyCollects(ActionEvent actionEvent) {
        refreshCurrentUser();
        setActiveButton(collectBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/showMyCollects.fxml"));
            Parent view = loader.load();

            // Get the controller and set the current user
            ShowMyCollectsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showLoadError("My Collects", e);
        }
    }


    public void showAllCollects(ActionEvent actionEvent) {
        refreshCurrentUser();
        setActiveButton(collectBtn);
        loadView("/com/example/livecycle/frontoffice/showAllCollects.fxml");
    }









    public void showAnnouncementShop(ActionEvent event) {
        setActiveButton(annonceBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/annonce_management.fxml"));
            Parent view = loader.load();

            AnnonceManagementController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setDashboardController(this);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showLoadError("Announcement Shop", e);
        }

    }


    public void showCommandManagement(ActionEvent actionEvent) throws IOException {
        refreshCurrentUser();
        setActiveButton(commandBtn);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/example/livecycle/frontoffice/commande_management.fxml"
        ));

        Parent cartPane = loader.load();

// ✏ *Right here, after loader.load():*
        CommandeManagementController cmc = loader.getController();
        cmc.initData(currentUser, this);

// Now swap it in:
        contentArea.getChildren().setAll(cartPane);

    }
    public void showForumManagement(ActionEvent actionEvent) {
        refreshCurrentUser();
        setActiveButton(forumBtn);
        loadView("/com/example/livecycle/frontoffice/UserCategoryView.fxml");
    }
    public void showEditUser() {
        deleteActiveButton();
        try {
            // Always get fresh data before showing edit form
            refreshCurrentUser();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/edit_profile.fxml"));
            Parent view = loader.load();

            EditProfileController controller = loader.getController();
            controller.initData(currentUser, this);

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
    @FXML
    private void showMyComplaints(ActionEvent event) {
        setActiveButton(reclamationBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/my_complaints.fxml"));
            Parent view = loader.load();

            MyComplaintsController controller = loader.getController();
            controller.setCurrentUser(currentUser); // Make sure currentUser is properly set

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            } else {
                showLoadError("My Complaints", new Exception("Content area not initialized"));
            }

        } catch (IOException e) {
            showLoadError("My Complaints", e);
        }
    }

    @FXML
    private void showCreateComplaint(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_complaint.fxml"));
            Parent view = loader.load();

            CreateComplaintController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            showLoadError("Create Complaint", e);
        }
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




    public void refreshCurrentUser() {
        User freshUser = userService.getUser(currentUser.getId());
        if (freshUser != null) {
            this.currentUser = freshUser;
        }
    }


    public void showMyAnnouncements(ActionEvent event) {

        if (hasRole("ROLE_ENTREPRISE")) {
            showLoadError("Access Denied", new Exception("Enterprise users cannot access My Announcements"));
            return;
        }




        setActiveButton(annonceBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/my_announcements.fxml"));
            Parent view = loader.load();

            MyAnnouncementsController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            showLoadError("My Announcements", e);
        }
    }



    private boolean hasRole(String role) {
        return currentUser != null &&
                currentUser.getRoles() != null &&
                currentUser.getRoles().contains(role);
    }




}
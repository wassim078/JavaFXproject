package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.User;
import com.example.livecycle.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EditProfileController {
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private Label rolesLabel;
    @FXML private VBox formContainer;
    @FXML private ImageView userImageView;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;



    @FXML private Label prenomError;
    @FXML private Label nomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label newPasswordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label adresseError;
    @FXML private Label verificationWarning;


    @FXML
    public void initialize() {
        setupImageClip();
    }

    private User currentUser;
    private final UserService userService = new UserService();
    private static final String UPLOADS_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String DEFAULT_AVATAR = "/com/example/livecycle/images/default-avatar.png";
    private UserDashboardController dashboardController;
    public static final Map<Integer, EditProfileController> openInstances = new ConcurrentHashMap<>();
    private static final String VERIFIED_STYLE = "verified-text";
    private static final String UNVERIFIED_STYLE = "warning-text";


    private void setupImageClip() {
        Circle clip = new Circle();
        clip.radiusProperty().bind(userImageView.fitWidthProperty().divide(2));
        clip.centerXProperty().bind(userImageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(userImageView.fitHeightProperty().divide(2));
        userImageView.setClip(clip);
    }



    public void initData(User user, UserDashboardController dashboardController) {
        User freshUser = userService.getUser(user.getId());
        this.currentUser = freshUser;
        this.dashboardController = dashboardController;
        populateForm();
        updateVerificationStatus();
        registerInstance();
    }



    private void registerInstance() {
        openInstances.put(currentUser.getId(), this);
    }

    private void unregisterInstance() {
        openInstances.remove(currentUser.getId());
    }

    public void refreshUserData() {
        User updatedUser = userService.getUser(currentUser.getId());
        if (updatedUser != null) {
            this.currentUser = updatedUser;
            Platform.runLater(() -> {
                populateForm();
                updateVerificationStatus();
            });
        }
    }
    private void updateVerificationStatus() {
        if (currentUser.isEnabled()) {
            verificationWarning.getStyleClass().remove(UNVERIFIED_STYLE);
            verificationWarning.getStyleClass().add(VERIFIED_STYLE);
            verificationWarning.setText("✓ Email verified successfully!");
        } else {
            verificationWarning.getStyleClass().remove(VERIFIED_STYLE);
            verificationWarning.getStyleClass().add(UNVERIFIED_STYLE);
            verificationWarning.setText("Your email is not verified! Please check your inbox.");
        }
        verificationWarning.setVisible(true);
    }

    private void populateForm() {
        prenomField.setText(currentUser.getPrenom());
        nomField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseField.setText(currentUser.getAdresse());
        rolesLabel.setText(currentUser.getFormattedRoles());
        loadUserImage();
    }

    private void loadUserImage() {
        try {
            String imagePath = currentUser.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Handle HTTP/HTTPS URLs
                if (imagePath.startsWith("http")) {
                    userImageView.setImage(new Image(imagePath));
                    return;
                }

                // Handle local files
                File imageFile = new File(UPLOADS_DIR + imagePath);
                if (imageFile.exists()) {
                    userImageView.setImage(new Image(imageFile.toURI().toString()));
                    return;
                }
            }
            // Load default avatar if no valid image found
            userImageView.setImage(new Image(DEFAULT_AVATAR));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            userImageView.setImage(new Image(DEFAULT_AVATAR));
        }
    }
    @FXML
    private void handleImageClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(userImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create uploads directory if not exists
                new File(UPLOADS_DIR).mkdirs();

                // Generate unique filename
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFilename = UUID.randomUUID() + extension;
                File destFile = new File(UPLOADS_DIR + newFilename);

                // Copy file
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update user and UI
                currentUser.setImage(newFilename);
                loadUserImage();

            } catch (IOException e) {
                showErrorAlert("File Error", "Could not save image: " + e.getMessage());
            }
        }
    }





    @FXML
    private void handleSaveChanges() {
        if (!validateForm() || !validatePassword()) return;

        updateUserFromForm();
        if (userService.updateUserProfile(currentUser)) {
            // Refresh both user data and avatar
            dashboardController.refreshCurrentUser();
            dashboardController.refreshUserAvatar();
            showSuccessAlert("Succès", "Profil mis à jour avec succès!");
        } else {
            showErrorAlert("Échec", "Erreur lors de la mise à jour");
        }
    }

    boolean validatePassword() {
        if (!newPasswordField.getText().isEmpty()) {
            if (!BCrypt.checkpw(currentPasswordField.getText(), currentUser.getPassword())) {
                showErrorAlert("Password Error", "Current password is incorrect");
                return false;
            }

            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showErrorAlert("Password Error", "New passwords do not match");
                return false;
            }

            currentUser.setPassword(BCrypt.hashpw(newPasswordField.getText(), BCrypt.gensalt()));
        }
        return true;
    }

    private void updateUserFromForm() {
        currentUser.setPrenom(prenomField.getText());
        currentUser.setNom(nomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setAdresse(adresseField.getText());
    }
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        unregisterInstance();
        dashboardController.refreshCurrentUser(); // Refresh parent controller's data
        Stage stage = (Stage) formContainer.getScene().getWindow();
        stage.close();
    }
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        // Validate first name
        if (prenomField.getText().isEmpty()) {
            prenomError.setText("Prénom est requis");
            isValid = false;
        }

        // Validate last name
        if (nomField.getText().isEmpty()) {
            nomError.setText("Nom est requis");
            isValid = false;
        }

        // Validate email
        if (emailField.getText().isEmpty()) {
            emailError.setText("Email est requis");
            isValid = false;
        } else if (!emailField.getText().equals(currentUser.getEmail()) &&
                userService.emailExists(emailField.getText())) {
            emailError.setText("Email déjà utilisé");
            isValid = false;
        }
        if (adresseField.getText().isEmpty()) {
            adresseError.setText("Adresse est requis");
            isValid = false;
        }
        // Validate phone number
        String phone = telephoneField.getText();
        if (!phone.matches("\\d{8}")) {
            telephoneError.setText("Numéro doit contenir 8 chiffres");
            isValid = false;
        }

        // Validate new password if provided
        if (!newPasswordField.getText().isEmpty()) {
            String password = newPasswordField.getText();
            if (password.length() < 4) {
                newPasswordError.setText("Minimum 4 caractères");
                isValid = false;
            }
            if (!password.matches(".*\\d.*") || !password.matches(".*[a-zA-Z].*")) {
                newPasswordError.setText("Doit contenir chiffres et lettres");
                isValid = false;
            }
            if (!password.equals(confirmPasswordField.getText())) {
                confirmPasswordError.setText("Les mots de passe ne correspondent pas");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        prenomError.setText("");
        nomError.setText("");
        emailError.setText("");
        telephoneError.setText("");
        newPasswordError.setText("");
        adresseError.setText("");
        confirmPasswordError.setText("");
    }


}
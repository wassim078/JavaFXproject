package com.example.livecycle.controllers.auth;

import com.example.livecycle.Main;
import com.example.livecycle.services.EmailService;
import com.example.livecycle.services.UserService;
import com.example.livecycle.entities.User;
import com.example.livecycle.utils.ValidationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Map;

public class RegisterController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField imagePathField;

    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label addressError;
    @FXML private Label phoneError;
    @FXML private Label roleError;
    @FXML private Label imageError;

    private String imageName;
    private static final String UPLOAD_DIR = "uploads" + File.separator;
    private static final String DEFAULT_AVATAR = "default-avatar.png";
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll(
                "ROLE_ENTREPRISE",
                "ROLE_CLIENT",
                "ROLE_LIVREUR"
        );
        ensureDefaultAvatarExists();
    }
    private void ensureDefaultAvatarExists() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            Path defaultPath = Paths.get(UPLOAD_DIR + DEFAULT_AVATAR);
            if (!Files.exists(defaultPath)) {
                try (InputStream is = getClass().getResourceAsStream("/com/example/livecycle/" + DEFAULT_AVATAR)) {
                    if (is != null) {
                        Files.copy(is, defaultPath);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing default avatar: " + e.getMessage());
        }
    }
    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        try {
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
                String uniqueFileName = generateUniqueFileName(selectedFile);
                String destinationPath = UPLOAD_DIR + uniqueFileName;

                Files.copy(
                        selectedFile.toPath(),
                        Paths.get(destinationPath),
                        StandardCopyOption.REPLACE_EXISTING
                );

                imageName = uniqueFileName;
                imagePathField.setText(uniqueFileName);
                imageError.setText("");
            }
        } catch (IOException e) {
            imageError.setText("Error uploading image: " + e.getMessage());
            imageName = null;
        }
    }

    @FXML
    private void handleRegister() {
        clearErrors();
        Map<String, String> errors = validateForm();

        if (!errors.isEmpty()) {
            displayErrors(errors);
            return;
        }

        // Use default avatar if no image was uploaded
        String finalImageName = (imageName != null && !imageName.isEmpty())
                ? imageName
                : DEFAULT_AVATAR;

        User user = new User(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim().toLowerCase(),
                passwordField.getText().trim(),
                addressField.getText().trim(),
                phoneField.getText().trim(),
                roleComboBox.getValue(),
                finalImageName
        );

        if (userService.createUserWithVerification(user)) {
            sendVerificationEmail(user);
            showAlert(Alert.AlertType.INFORMATION,
                    "Registration Successful",
                    "Verification email sent! Please check your inbox.");
            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Registration Failed",
                    "Could not complete registration. Please try again.");
        }
    }

    private Map<String, String> validateForm() {
        Map<String, String> errors = ValidationUtils.validateRegistration(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                passwordField.getText().trim(),
                confirmPasswordField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
        );

        if (userService.emailExists(emailField.getText().trim())) {
            errors.put("email", "Email already registered");
        }
        if (roleComboBox.getValue() == null) {
            errors.put("role", "Please select a role");
        }

        return errors;
    }

    private void displayErrors(Map<String, String> errors) {
        firstNameError.setText(errors.getOrDefault("firstName", ""));
        lastNameError.setText(errors.getOrDefault("lastName", ""));
        emailError.setText(errors.getOrDefault("email", ""));
        passwordError.setText(errors.getOrDefault("password", ""));
        confirmPasswordError.setText(errors.getOrDefault("confirmPassword", ""));
        phoneError.setText(errors.getOrDefault("phone", ""));
        addressError.setText(errors.getOrDefault("address", ""));
        roleError.setText(errors.getOrDefault("role", ""));
    }

    private void clearErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        addressError.setText("");
        phoneError.setText("");
        roleError.setText("");
        imageError.setText("");
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        addressField.clear();
        phoneField.clear();
        imagePathField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }

    private String generateUniqueFileName(File file) {
        String originalName = file.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String extension = dotIndex > 0 ? originalName.substring(dotIndex) : "";
        return UUID.randomUUID() + extension;
    }

    @FXML
    private void handleLoginNavigation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/login.fxml"));
            Parent root = loader.load();

            // Retrieve the LoginController and inject HostServices from Main
            LoginController loginController = loader.getController();
            loginController.setHostServices(Main.getAppHostServices());

            // Get the current stage from the event source and set the new scene
            Stage stage = (Stage) ((Control) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 700, 700);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load login form", ButtonType.OK).show();
        }
    }


    private void sendVerificationEmail(User user) {
        new Thread(() -> {
            try {
                String verificationLink = "http://localhost:8082/verify?token=" +
                        user.getVerificationToken();
                emailService.sendVerificationEmail(user.getEmail(), verificationLink);
            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR,
                                "Email Error",
                                "Failed to send verification email: " + e.getMessage())
                );
            }
        }).start();
    }


}
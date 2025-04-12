package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.UserService;
import com.example.livecycle.entities.User;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserCreateController {

    // Form Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private StackPane imageContainer;
    @FXML private Label imageError;

    // Error Labels
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label addressError;
    @FXML private Label passwordError;
    @FXML private Label roleError;
    @FXML private ImageView userImageView;




    private final SimpleBooleanProperty formValid = new SimpleBooleanProperty(false);
    private String imagePath;
    private final UserService userService = new UserService();
    private static final String UPLOAD_DIR = "uploads/";
    private User newUser;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{4,}$");
    private static final int PASSWORD_MIN_LENGTH = 4;


    @FXML
    public void initialize() {
        newUser = new User(); // Requires empty constructor
        initializeRoleComboBox();
        setupValidations();
        createUploadsDirectory();
        Circle clip = new Circle(50, 50, 50); // CenterX, CenterY, Radius
        userImageView.setClip(clip);

        imagePath = "default-avatar.png";
        File imageFile = new File(UPLOAD_DIR + imagePath);
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            userImageView.setImage(image);
        } else {
            showError(imageError, "Default image not found.");
        }
        userImageView.setPreserveRatio(false);
        userImageView.setSmooth(true);
    }

    private void createUploadsDirectory() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Check and copy default avatar if missing
            Path defaultAvatarPath = Paths.get(UPLOAD_DIR + "default-avatar.png");
            if (!Files.exists(defaultAvatarPath)) {
                InputStream is = getClass().getResourceAsStream("/images/default-avatar.png");
                if (is != null) {
                    Files.copy(is, defaultAvatarPath);
                } else {
                    showError(imageError, "Default avatar not found in resources.");
                }
            }
        } catch (IOException e) {
            showError(imageError, "Failed to create directory or copy avatar: " + e.getMessage());
        }
    }

    private void initializeRoleComboBox() {
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN", "ROLE_ENTREPRISE");
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void setupValidations() {


        setupRequiredFieldValidation(firstNameField, firstNameError, "First name cannot be empty");
        setupRequiredFieldValidation(lastNameField, lastNameError, "Last name cannot be empty");
        setupRequiredFieldValidation(emailField, emailError, "Email cannot be empty");
        setupRequiredFieldValidation(phoneField, phoneError, "Phone cannot be empty");
        setupRequiredFieldValidation(addressField, addressError, "Address cannot be empty");
        setupRequiredFieldValidation(passwordField, passwordError, "Password cannot be empty");



        // Add email existence check to validation
        BooleanBinding validFields = firstNameField.textProperty().isNotEmpty()
                .and(lastNameField.textProperty().isNotEmpty())
                .and(emailField.textProperty().isNotEmpty())
                .and(phoneField.textProperty().isNotEmpty())
                .and(addressField.textProperty().isNotEmpty())
                .and(passwordField.textProperty().isNotEmpty())
                .and(validEmailFormat())
                .and(validEmailNotExists())
                .and(validPhone())
                .and(validPassword());

        formValid.bind(validFields);


        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                emailError.setText("");
            } else if (!newVal.matches(".+@.+\\..+")) {
                emailError.setText("Invalid email format");
            } else if (userService.emailExists(newVal)) {
                emailError.setText("Email already registered");
            } else {
                emailError.setText("");
            }
        });
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                passwordError.setText("Password cannot be empty");
            } else if (newVal.length() < 4) {
                passwordError.setText("Password must be at least 4 characters");
            } else if (!newVal.matches(".*[a-zA-Z].*") || !newVal.matches(".*\\d.*")) {
                passwordError.setText("Password must contain both letters and digits");
            } else {
                passwordError.setText("");
            }
        });
    }
    private BooleanBinding validPassword() {
        return Bindings.createBooleanBinding(() -> {
            String password = passwordField.getText();
            if (password.isEmpty()) return false;
            if (password.length() < 4) return false;
            return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
        }, passwordField.textProperty());
    }

    private void setupRequiredFieldValidation(TextField field, Label errorLabel, String errorMessage) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                errorLabel.setText(errorMessage);
            } else {
                errorLabel.setText("");
            }
        });

        // Initial check
        if (field.getText().trim().isEmpty()) {
            errorLabel.setText(errorMessage);
        }
    }

    // Overload for PasswordField
    private void setupRequiredFieldValidation(PasswordField field, Label errorLabel, String errorMessage) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                errorLabel.setText(errorMessage);
            } else {
                errorLabel.setText("");
            }
        });

        // Initial check
        if (field.getText().trim().isEmpty()) {
            errorLabel.setText(errorMessage);
        }
    }



    private BooleanBinding validPhone() {
        return Bindings.createBooleanBinding(() ->
                        phoneField.getText().matches("\\d{8}"),
                phoneField.textProperty()
        );
    }


    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(userImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String uniqueFileName = UUID.randomUUID() + "_" + Instant.now().getEpochSecond() + getFileExtension(selectedFile.getName());
                Path target = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.copy(selectedFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                imagePath = uniqueFileName;
                newUser.setImage(imagePath);
                updateImagePreview(target.toString());
                clearError(imageError);

            } catch (IOException e) {
                showError(imageError, "Error uploading image: " + e.getMessage());
            }
        }
    }

    private void updateImagePreview(String imagePath) {
        Image image = new Image(new File(imagePath).toURI().toString());
        userImageView.setImage(image);
    }

    public User getNewUser() {
        // Set all fields from form inputs
        newUser.setPrenom(firstNameField.getText());
        newUser.setNom(lastNameField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setAdresse(addressField.getText());
        newUser.setTelephone(phoneField.getText());
        newUser.setRoles("[\"" + roleComboBox.getValue() + "\"]");
        newUser.setImage(imagePath);

        return newUser;
    }

    public BooleanProperty formValidProperty() {
        return formValid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
    private BooleanBinding validEmailFormat() {
        return Bindings.createBooleanBinding(() ->
                        emailField.getText().matches(".+@.+\\..+"),
                emailField.textProperty()
        );
    }
    private BooleanBinding validEmailNotExists() {
        return Bindings.createBooleanBinding(() ->
                        !userService.emailExists(emailField.getText()),
                emailField.textProperty()
        );
    }
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}
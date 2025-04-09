package com.example.livecycle.controllers;

import com.example.livecycle.dao.UserDAO;
import com.example.livecycle.models.User;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

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

    private final SimpleBooleanProperty formValid = new SimpleBooleanProperty(false);
    private String imagePath;
    private final UserDAO userDAO = new UserDAO();
    private static final String UPLOAD_DIR = "uploads/";
    private User newUser;
    @FXML
    public void initialize() {
        newUser = new User(); // Requires empty constructor
        initializeRoleComboBox();
        setupValidations();
        createUploadsDirectory();
    }

    private void createUploadsDirectory() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            showError(imageError, "Failed to create uploads directory");
        }
    }

    private void initializeRoleComboBox() {
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN", "ROLE_ENTREPRISE");
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void setupValidations() {
        // Add email existence check to validation
        BooleanBinding validFields = firstNameField.textProperty().isNotEmpty()
                .and(lastNameField.textProperty().isNotEmpty())
                .and(emailField.textProperty().isNotEmpty())
                .and(passwordField.textProperty().isNotEmpty())
                .and(phoneField.textProperty().isNotEmpty())
                .and(addressField.textProperty().isNotEmpty())
                .and(validEmailFormat())
                .and(validEmailNotExists())
                .and(validPhone());

        formValid.bind(validFields);
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                emailError.setText("");
            } else if (!newVal.matches(".+@.+\\..+")) {
                emailError.setText("Invalid email format");
            } else if (userDAO.emailExists(newVal)) {
                emailError.setText("Email already registered");
            } else {
                emailError.setText("");
            }
        });
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

        File selectedFile = fileChooser.showOpenDialog(imageContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String uniqueFileName = UUID.randomUUID() + "_" +
                        Instant.now().getEpochSecond() +
                        getFileExtension(selectedFile.getName());

                Path target = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.copy(selectedFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                // Update both the temp field and User object
                imagePath = uniqueFileName;
                newUser.setImage(uniqueFileName);

                updateImagePreview(target.toString());
                clearError(imageError);

            } catch (IOException e) {
                showError(imageError, "Error uploading image: " + e.getMessage());
            }
        }
    }

    private void updateImagePreview(String imagePath) {
        Image image = new Image(new File(imagePath).toURI().toString());
        BackgroundImage bgImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        );
        imageContainer.setBackground(new Background(bgImage));
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
                        !userDAO.emailExists(emailField.getText()),
                emailField.textProperty()
        );
    }
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}
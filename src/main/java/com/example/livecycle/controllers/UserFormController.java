package com.example.livecycle.controllers;

import com.example.livecycle.models.User;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class UserFormController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label imageError;
    @FXML private Label phoneError;
    @FXML private Label emailError;
    @FXML private StackPane imageContainer;

    private final BooleanProperty formValid = new SimpleBooleanProperty(false);
    private User existingUser;
    private String imageName;
    private static final String UPLOAD_DIR = "uploads" + File.separator;
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d*");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int PHONE_MAX_LENGTH = 8;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_ADMIN", "ROLE_ENTREPRISE", "ROLE_USER");
        configurePhoneField();
        configureEmailField();

        BooleanBinding fieldsFilled = firstNameField.textProperty().isNotEmpty()
                .and(lastNameField.textProperty().isNotEmpty())
                .and(emailField.textProperty().isNotEmpty())
                .and(addressField.textProperty().isNotEmpty())
                .and(emailValidProperty())
                .and(phoneValidProperty());

        formValid.bind(fieldsFilled);
    }

    private void configurePhoneField() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return PHONE_PATTERN.matcher(newText).matches() && newText.length() <= PHONE_MAX_LENGTH ? change : null;
        };
        phoneField.setTextFormatter(new TextFormatter<>(filter));

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            phoneError.setText(!newVal.isEmpty() && newVal.length() != PHONE_MAX_LENGTH ? "Phone must be 8 digits" : "");
        });
    }

    private void configureEmailField() {
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateEmail();
        });
    }

    public BooleanProperty formValidProperty() {
        return formValid;
    }

    private BooleanProperty emailValidProperty() {
        BooleanProperty emailValid = new SimpleBooleanProperty(true);
        emailField.textProperty().addListener((obs, oldVal, newVal) ->
                emailValid.set(EMAIL_PATTERN.matcher(newVal).matches() || newVal.isEmpty()));
        return emailValid;
    }

    private void validateEmail() {
        String email = emailField.getText();
        emailError.setText(!EMAIL_PATTERN.matcher(email).matches() && !email.isEmpty() ? "Invalid email format" : "");
    }

    private BooleanProperty phoneValidProperty() {
        BooleanProperty phoneValid = new SimpleBooleanProperty(true);
        phoneField.textProperty().addListener((obs, oldVal, newVal) ->
                phoneValid.set(newVal.length() == PHONE_MAX_LENGTH));
        return phoneValid;
    }

    public void setUser(User user) {
        this.existingUser = user;
        firstNameField.setText(user.getPrenom());
        lastNameField.setText(user.getNom());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getTelephone());
        addressField.setText(user.getAdresse());
        passwordField.setText("");

        String rolesStr = user.getRoles();
        String role = rolesStr.replaceAll("[\\[\\]\"]", "");
        roleComboBox.getSelectionModel().select(role);

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            File imageFile = new File(UPLOAD_DIR + user.getImage());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString(), 120, 120, true, true);
                BackgroundImage bgImage = new BackgroundImage(image,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER, new BackgroundSize(120, 120, false, false, false, false));
                imageContainer.setBackground(new Background(bgImage));
            } else {
                System.err.println("Image file not found: " + imageFile.getAbsolutePath());
            }
        }
    }

    public User getUser() {
        existingUser.setPrenom(firstNameField.getText());
        existingUser.setNom(lastNameField.getText());
        existingUser.setEmail(emailField.getText());
        existingUser.setTelephone(phoneField.getText());
        existingUser.setAdresse(addressField.getText());

        if (!passwordField.getText().isEmpty()) {
            existingUser.setPassword(passwordField.getText());
        }

        String selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
        existingUser.setRoles("[\"" + selectedRole + "\"]");

        if (imageName != null) {
            existingUser.setImage(imageName);
        }

        return existingUser;
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(imageContainer.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
                String uniqueFileName = generateUniqueFileName(selectedFile);
                Files.copy(selectedFile.toPath(),
                        Paths.get(UPLOAD_DIR + uniqueFileName),
                        StandardCopyOption.REPLACE_EXISTING);

                imageName = uniqueFileName;
                imageError.setText("");

                Image image = new Image(new File(UPLOAD_DIR + uniqueFileName).toURI().toString(),
                        100, 100, false, true);
                BackgroundImage bgImage = new BackgroundImage(image,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER, new BackgroundSize(100, 100, false, false, false, false));
                imageContainer.setBackground(new Background(bgImage));

            } catch (IOException e) {
                imageError.setText("Error uploading image: " + e.getMessage());
                imageName = null;
            }
        }
    }

    private String generateUniqueFileName(File file) {
        String originalName = file.getName();
        int dotIndex = originalName.lastIndexOf('.');
        String extension = (dotIndex > 0) ? originalName.substring(dotIndex) : "";
        return UUID.randomUUID() + "_" + Instant.now().getEpochSecond() + extension;
    }
}
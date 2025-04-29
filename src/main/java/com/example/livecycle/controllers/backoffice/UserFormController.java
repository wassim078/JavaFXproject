package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.User;
import com.example.livecycle.services.UserService;
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
    @FXML private HBox imageContainer;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label addressError;
    @FXML private Label passwordError;
    @FXML private ImageView userImageView;



    private final BooleanProperty formValid = new SimpleBooleanProperty(false);
    private User existingUser;
    private String imageName;
    private static final String UPLOAD_DIR = "uploads" + File.separator;
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d*");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int PHONE_MAX_LENGTH = 8;
    private final UserService userService = new UserService();
    private boolean isNewUser = true;
    private static final int PASSWORD_MIN_LENGTH = 4;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).+$"); // At least 1 letter and 1 digit


    @FXML
    public void initialize() {
        Circle clip = new Circle();
        clip.centerXProperty().bind(userImageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(userImageView.fitHeightProperty().divide(2));
        clip.radiusProperty().bind(userImageView.fitWidthProperty().divide(2));
        userImageView.setClip(clip);



        roleComboBox.getItems().addAll("ROLE_ADMIN", "ROLE_ENTREPRISE", "ROLE_USER");
        setupFieldValidations();
        updateFormValidation();
    }
    private void setupFieldValidations() {
        setupRequiredFieldValidation(firstNameField, firstNameError);
        setupRequiredFieldValidation(lastNameField, lastNameError);
        setupRequiredFieldValidation(addressField, addressError);
        setupEmailValidation();
        setupPhoneValidation();
        setupPasswordValidation();
    }

    private void setupRequiredFieldValidation(TextField field, Label errorLabel) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                errorLabel.setText("This field is required");
            } else {
                errorLabel.setText("");
            }
        });
    }

    private void setupEmailValidation() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                emailError.setText("Email is required");
            } else if (!EMAIL_PATTERN.matcher(newVal).matches()) {
                emailError.setText("Invalid email format");
            } else if (isEmailAlreadyExists(newVal)) {
                emailError.setText("Email already exists");
            } else {
                emailError.setText("");
            }
        });
    }
    private void setupPhoneValidation() {
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                phoneError.setText("Phone is required");
            } else if (newVal.length() != PHONE_MAX_LENGTH) {
                phoneError.setText("Must be 8 digits");
            } else {
                phoneError.setText("");
            }
        });
    }
    private void setupPasswordValidation() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                if (newVal.length() < PASSWORD_MIN_LENGTH) {
                    passwordError.setText("Min 4 characters");
                } else if (!PASSWORD_PATTERN.matcher(newVal).matches()) {
                    passwordError.setText("Must contain letters and digits");
                } else {
                    passwordError.setText("");
                }
            } else {
                passwordError.setText("");
            }
        });
    }


    private boolean isEmailAlreadyExists(String email) {
        if (existingUser != null && email.equals(existingUser.getEmail())) {
            return false; // Allow current email
        }
        return userService.emailExists(email);
    }


    private void updateFormValidation() {
        BooleanBinding fieldsFilled = firstNameField.textProperty().isNotEmpty()
                .and(lastNameField.textProperty().isNotEmpty())
                .and(emailField.textProperty().isNotEmpty())
                .and(phoneField.textProperty().isNotEmpty())
                .and(addressField.textProperty().isNotEmpty())
                .and(emailValidProperty())
                .and(phoneValidProperty());

        BooleanBinding emailUnique = Bindings.createBooleanBinding(() ->
                        !isEmailAlreadyExists(emailField.getText()),
                emailField.textProperty()
        );

        BooleanBinding passwordValid = Bindings.createBooleanBinding(() ->
                        passwordField.getText().isEmpty() ||
                                (passwordField.getText().length() > PASSWORD_MIN_LENGTH &&
                                        PASSWORD_PATTERN.matcher(passwordField.getText()).matches()),
                passwordField.textProperty()
        );

        formValid.bind(fieldsFilled.and(emailUnique).and(passwordValid));
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
        this.isNewUser = (user.getId() == 0);
        firstNameField.setText(user.getPrenom());
        lastNameField.setText(user.getNom());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getTelephone());
        addressField.setText(user.getAdresse());
        passwordField.setText("");

        String rolesStr = user.getRoles();
        String role = rolesStr.replaceAll("[\\[\\]\"]", "");
        roleComboBox.getSelectionModel().select(role);

        try {
            Image image;
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                if (user.getImage().startsWith("http")) {
                    // Handle URL-based images (Google account)
                    image = new Image(user.getImage(), 100, 100, true, true);
                } else {
                    // Handle local file images
                    File imageFile = new File(UPLOAD_DIR + user.getImage());
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString(), 100, 100, true, true);
                    } else {
                        // Fallback to default image
                        image = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-avatar.png"));
                    }
                }
            } else {
                // Load default image from resources
                image = new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-avatar.png"));
            }

            userImageView.setImage(image);

        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Fallback to embedded default image
            userImageView.setImage(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/default-avatar.png")));
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

                Image image = new Image(new File(UPLOAD_DIR + uniqueFileName).toURI().toString(),100, 100, true, true);
                userImageView.setImage(image); // Update ImageView directly

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
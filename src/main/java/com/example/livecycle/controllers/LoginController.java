package com.example.livecycle.controllers;

import com.example.livecycle.dao.UserDAO;
import com.example.livecycle.models.User;
import com.example.livecycle.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @FXML
    private void handleLogin() throws IOException {
        clearErrors();

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        Map<String, String> errors = ValidationUtils.validateLogin(email, password);

        if (!errors.isEmpty()) {
            emailError.setText(errors.getOrDefault("email", ""));
            passwordError.setText(errors.getOrDefault("password", ""));
            return;
        }

        User user = userDAO.authenticateUser(email, password);

        if (user != null) {
            redirectBasedOnRole(user);
        } else {
            passwordError.setText("Invalid email or password");
        }
    }

    private void redirectBasedOnRole(User user) throws IOException {
        // Parse JSON roles array
        List<String> roles = gson.fromJson(user.getRoles(), new TypeToken<List<String>>(){}.getType());

        String fxmlPath;
        if (roles.contains("ROLE_ADMIN")) {
            fxmlPath = "/com/example/livecycle/admin_dashboard.fxml";
        } else {
            fxmlPath = "/com/example/livecycle/user_dashboard.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Initialize the correct controller based on role
        if (roles.contains("ROLE_ADMIN")) {
            AdminDashboardController controller = loader.getController();
            controller.initData(user);
        } else {
            UserDashboardController controller = loader.getController();
            controller.initData(user);  // Make sure this method exists in UserDashboardController
        }

        Stage stage = (Stage) emailField.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("LiveCycle Dashboard");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    private void clearErrors() {
        emailError.setText("");
        passwordError.setText("");
    }


    @FXML
    private void handleRegisterNavigation(javafx.event.ActionEvent event) {
        try {
            Hyperlink source = (Hyperlink) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/livecycle/register.fxml"));
            Scene scene = new Scene(root, 700, 600); // Set width and height here
            stage.setScene(scene);
            stage.setTitle("LiveCycle Dashboard");

            // Optional: Set minimum size constraints
            stage.setMinWidth(700);
            stage.setMinHeight(600);

            // Optional: Center window on screen
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load registration form");
            alert.showAndWait();
        }
    }

}
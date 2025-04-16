package com.example.livecycle.controllers.auth;

import com.example.livecycle.services.EmailService;
import com.example.livecycle.services.UserService;
import com.example.livecycle.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Label emailError;

    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();





    @FXML
    private void handleResetRequest() {
        String email = emailField.getText().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            emailError.setText("Please enter a valid email address");
            return;
        }

        if (!userService.emailExists(email)) {
            emailError.setText("No account found with this email");
            return;
        }

        String resetToken = UUID.randomUUID().toString();
        userService.createPasswordResetToken(email, resetToken);

        String resetLink = "http://localhost:8082/reset?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetLink);

        showAlert("Reset Link Sent", "Check your email for password reset instructions");
    }

    @FXML
    private void handleBackToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showAlert(String title, String message) {
        // Your existing alert implementation
    }
}
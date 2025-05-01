package com.example.livecycle.controllers.auth;

import com.example.livecycle.services.UserService;
import com.example.livecycle.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ResetPasswordController {
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label newPasswordError;
    @FXML private Label confirmError;

    private final UserService userService = new UserService();
    private String resetToken;

    public void setResetToken(String token) {
        this.resetToken = token;
    }

    @FXML
    private void handlePasswordReset() {
        String password = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!ValidationUtils.isValidPassword(password)) {
            newPasswordError.setText("Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirm)) {
            confirmError.setText("Passwords do not match");
            return;
        }

        if (userService.resetPassword(resetToken, password)) {
            showAlert("Success", "Password has been reset successfully");
            // Close the reset window
            ((Stage) newPasswordField.getScene().getWindow()).close();
        } else {
            showAlert("Error", "Invalid or expired reset token");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
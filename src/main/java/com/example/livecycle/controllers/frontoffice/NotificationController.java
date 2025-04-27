package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class NotificationController {
    @FXML private Label verificationStatus;

    public void updateVerificationStatus(User user) {
        Platform.runLater(() -> {
            if (user.isEnabled()) {
                verificationStatus.setText("Email verified! ✔️");
                verificationStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 40px;");
            } else {
                verificationStatus.setText("Email not verified! ✖️\n(Check your inbox)");
                verificationStatus.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 40px;");
            }
            // Ensure text wrapping and centering
            verificationStatus.setWrapText(true);
            verificationStatus.setAlignment(Pos.CENTER);
        });
    }
}
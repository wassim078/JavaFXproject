package com.example.livecycle.controllers;

import com.example.livecycle.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProfileViewController {
    @FXML private ImageView profileImage;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label roleLabel;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String DEFAULT_IMAGE = "/com/example/livecycle/default-avatar.png";

    public void initializeUserData(User user) {
        firstNameLabel.setText(user.getPrenom());
        lastNameLabel.setText(user.getNom());
        emailLabel.setText(user.getEmail());
        phoneLabel.setText(user.getTelephone());
        addressLabel.setText(user.getAdresse());
        roleLabel.setText(user.getRoles().replaceAll("[\\[\\]\"]", ""));

        try {
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                profileImage.setImage(new Image("file:" + UPLOAD_DIR + user.getImage()));
            } else {
                profileImage.setImage(new Image(DEFAULT_IMAGE));
            }
        } catch (Exception e) {
            profileImage.setImage(new Image(DEFAULT_IMAGE));
        }
    }
}

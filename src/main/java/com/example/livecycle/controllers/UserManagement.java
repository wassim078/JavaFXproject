package com.example.livecycle.controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import com.example.livecycle.dao.UserDAO;
import com.example.livecycle.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagement {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> profileColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> addressColumn;
    @FXML private TableColumn<User, String> rolesColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final String DEFAULT_IMAGE_PATH = "/com/example/livecycle/default-avatar.png";
    static {
        new File(UPLOAD_DIR).mkdirs();
    }
    @FXML
    public void initialize() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setItems(users); // Directly bind the list
        setupTableColumns();
        loadUsers();
    }

    private void setupTableColumns() {
        // Set up cell value factories
        profileColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        rolesColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
        // Set up custom cell factories
        profileColumn.setCellFactory(createProfileCellFactory());
        actionsColumn.setCellFactory(createActionsCellFactory());
    }

    private Callback<TableColumn<User, String>, TableCell<User, String>> createProfileCellFactory() {
        return column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Image defaultImage = new Image(
                    getClass().getResourceAsStream(DEFAULT_IMAGE_PATH)
            );

            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                try {
                    if (imageName == null || imageName.isEmpty()) {
                        imageView.setImage(defaultImage);
                    } else {
                        File imageFile = new File(UPLOAD_DIR + imageName);
                        if (imageFile.exists()) {
                            imageView.setImage(new Image(imageFile.toURI().toString()));
                        } else {
                            imageView.setImage(defaultImage);
                        }
                    }
                    setGraphic(imageView);
                } catch (Exception e) {
                    imageView.setImage(defaultImage);
                    setGraphic(imageView);
                }
            }
        };
    }


    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createActionsCellFactory() {
        return column -> new TableCell<>() {
            private final HBox buttons = new HBox(8);

            // Initialize buttons separately
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                // Style buttons
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");

                // Set button actions
                editBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleDeleteUser(user);
                    }
                });

                // Add buttons to HBox
                buttons.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        };
    }

    private void loadUsers() {
        List<User> userList = userDAO.getAllUsers();
        System.out.println("Total users from DB: " + userList.size());

        List<User> filteredList = userList.stream()
                .filter(u -> {
                    String roles = u.getRoles().replaceAll("\\s", ""); // Remove whitespace
                    return !roles.contains("\"ROLE_ADMIN\"");
                })
                .collect(Collectors.toList());

        users.setAll(filteredList);
        userTable.setItems(users); // Explicitly set items
        userTable.refresh(); // Force refresh
        System.out.println("Filtered non-admin users: " + users.size());
    }

    @FXML
    private void handleCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/user_create_form.fxml"));
            Parent form = loader.load();
            UserCreateController controller = loader.getController();

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Create New User");
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);


            dialog.setOnShown(e -> {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                try (InputStream iconStream = getClass().getResourceAsStream("/com/example/livecycle/logo.png")) {
                    if (iconStream != null) {
                        stage.getIcons().add(new Image(iconStream));
                    } else {
                        System.err.println("Logo image not found!");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });


            // Bind validation if needed
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.disableProperty().bind(controller.formValidProperty().not());

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getNewUser();
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();

            // Add the refresh code HERE
            result.ifPresent(user -> {
                if (userDAO.addUser(user)) {
                    loadUsers(); // Refresh the list properly
                    userTable.refresh();
                    showAlert("Success", "User created successfully!");
                }
            });

        } catch (IOException e) {
            showAlert("Error", "Failed to load form");
        }
    }


    private void handleEditUser(User user) {
        if (user != null) {
            try {
                // Load the FXML for the user form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/user_form.fxml"));
                Parent form = loader.load();

                // Get the form controller and pass in the selected user's data
                UserFormController formController = loader.getController();
                formController.setUser(user);  // Pre-fill form fields including role

                // Create a dialog to display the form
                Dialog<User> dialog = new Dialog<>();
                dialog.setTitle("Edit User");
                dialog.getDialogPane().setContent(form);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // If your form has validation, bind the OK button's disable property
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.disableProperty().bind(formController.formValidProperty().not());

                // Convert the result when OK is clicked
                dialog.setResultConverter(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        return formController.getUser();  // Return the updated user
                    }
                    return null;
                });

                Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/logo.png")));

                // Show the dialog and update the user if the result is present
                Optional<User> result = dialog.showAndWait();
                result.ifPresent(updatedUser -> {
                    if (userDAO.updateUser(updatedUser)) {
                        // Update the user in the ObservableList so the table reflects changes
                        int index = users.indexOf(user);
                        if (index != -1) {
                            users.set(index, updatedUser);
                        }
                        userTable.refresh();
                        showAlert("Success", "User updated successfully!");
                    } else {
                        showAlert("Error", "Failed to update user.");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load the edit form.");
            }
        }
    }



    private void handleViewUser(User user) {
        if (user != null) {
            // Open view dialog
        }
    }

    private void handleDeleteUser(User user) {
        if (user != null) {
            if (userDAO.deleteUser(user.getId())) {
                users.remove(user);
                showAlert("Success", "User deleted successfully");
            }
        }
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK)
                .showAndWait();
    }
    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
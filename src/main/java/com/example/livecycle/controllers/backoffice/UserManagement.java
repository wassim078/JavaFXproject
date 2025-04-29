package com.example.livecycle.controllers.backoffice;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import com.example.livecycle.services.UserService;
import com.example.livecycle.entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
    @FXML private TableColumn<User, Void> editDeleteColumn;
    @FXML private TableColumn<User, Void> banUnbanColumn;

    private final UserService userService = new UserService();
    private ObservableList<User> users = FXCollections.observableArrayList();
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;



    private static final String DEFAULT_IMAGE_PATH = "/com/example/livecycle/images/default-avatar.png";
    static {
        new File(UPLOAD_DIR).mkdirs();
    }

    @FXML
    public void initialize() throws SQLException {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setItems(users); // Directly bind the list
        setupTableColumns();
        loadUsers();
        userTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (user == null || empty) {
                    setStyle("");
                } else {
                    if (user.isBanned()) {
                        setStyle("-fx-background-color: #ffebee;"); // Light red
                    } else {
                        setStyle("");
                    }
                }
            }
        });
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
        editDeleteColumn.setCellFactory(createEditDeleteCellFactory());
        banUnbanColumn.setCellFactory(createBanUnbanCellFactory());
    }





    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createBanUnbanCellFactory() {
        return column -> new TableCell<>() {
            private final HBox buttons = new HBox(4);
            private final Button banBtn = new Button("Ban");
            private final Button unbanBtn = new Button("Unban");

            {
                banBtn.getStyleClass().add("ban-button");
                unbanBtn.getStyleClass().add("unban-button");
                banBtn.setTooltip(new Tooltip("Ban User"));
                unbanBtn.setTooltip(new Tooltip("Unban User"));

                // Add action handlers if needed
                banBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                         handleBanUser(user); // Placeholder for ban logic
                    }
                });

                unbanBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleUnbanUser(user); // Placeholder for unban logic
                    }
                });

                buttons.getChildren().addAll(banBtn, unbanBtn);
                buttons.setAlignment(Pos.CENTER); // Center buttons in the column
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User user = getTableRow().getItem();
                    if (user.isBanned()) {
                        buttons.getChildren().setAll(unbanBtn);
                    } else {
                        buttons.getChildren().setAll(banBtn);
                    }
                    setGraphic(buttons);
                }
            }
        };
    }


    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createEditDeleteCellFactory() {
        return column -> new TableCell<>() {
            private final HBox buttons = new HBox(4);
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                editBtn.setTooltip(new Tooltip("Edit User"));
                deleteBtn.setTooltip(new Tooltip("Delete User"));

                editBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            handleDeleteUser(user);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                buttons.getChildren().addAll(editBtn, deleteBtn);
                buttons.setAlignment(Pos.CENTER); // Center buttons in the column
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









    private Callback<TableColumn<User, String>, TableCell<User, String>> createProfileCellFactory() {
        return column -> new TableCell<>() {
            private final StackPane container = new StackPane();
            private final ImageView imageView = new ImageView();
            private final Circle clip = new Circle();
            private final Image defaultImage = new Image(
                    getClass().getResourceAsStream(DEFAULT_IMAGE_PATH)
            );

            {
                // Container setup
                container.setPrefSize(50, 50);
                container.setMaxSize(50, 50);

                // Dynamic clipping
                clip.radiusProperty().bind(container.widthProperty().divide(2));
                clip.centerXProperty().bind(container.widthProperty().divide(2));
                clip.centerYProperty().bind(container.heightProperty().divide(2));
                container.setClip(clip);

                // ImageView setup
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.fitWidthProperty().bind(container.widthProperty());
                imageView.fitHeightProperty().bind(container.heightProperty());
                imageView.getStyleClass().add("table-avatar");

                container.getChildren().add(imageView);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                try {
                    Image image = loadImage(imageName);
                    imageView.setImage(image);
                    setGraphic(container);
                } catch (Exception e) {
                    imageView.setImage(defaultImage);
                    setGraphic(container);
                }
            }

            private Image loadImage(String imageName) {
                if (imageName == null || imageName.isEmpty()) {
                    return defaultImage;
                }

                try {
                    if (imageName.startsWith("http")) {
                        return new Image(imageName,
                                60, 60, // Load at higher resolution
                                true,    // Preserve ratio
                                true,    // Smooth scaling
                                true     // Background loading
                        );
                    } else {
                        File imageFile = new File(UPLOAD_DIR + imageName);
                        if (imageFile.exists()) {
                            return new Image(imageFile.toURI().toString(),
                                    60, 60, true, true, true
                            );
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defaultImage;
            }
        };
    }

    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createActionsCellFactory() {
        return column -> new TableCell<>() {
            private final HBox buttons = new HBox(4); // Reduced spacing from 8 to 4

            // Initialize buttons separately
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button banBtn = new Button("Ban");
            private final Button unbanBtn = new Button("Unban");

            {
                // Style buttons
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                banBtn.getStyleClass().add("ban-button");
                unbanBtn.getStyleClass().add("unban-button");

                // Add tooltips for clarity
                editBtn.setTooltip(new Tooltip("Edit User"));
                deleteBtn.setTooltip(new Tooltip("Delete User"));
                banBtn.setTooltip(new Tooltip("Ban User"));
                unbanBtn.setTooltip(new Tooltip("Unban User"));

                // Set button actions for existing buttons
                editBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            handleDeleteUser(user);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                // No actions for banBtn and unbanBtn as per request

                // Add buttons to HBox
                buttons.getChildren().addAll(editBtn, deleteBtn, banBtn, unbanBtn);
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

    private void loadUsers() throws SQLException {
        List<User> userList = userService.recuperer();
        System.out.println("Total users from DB: " + userList.size());

        List<User> filteredList = userList.stream()
                .filter(u -> {
                    String roles = u.getRoles().replaceAll("\\s", ""); // Remove whitespace
                    return !roles.contains("\"ROLE_ADMIN\"");
                })
                .collect(Collectors.toList());

        users.setAll(filteredList);
        userTable.refresh(); // Force refresh
        System.out.println("Filtered non-admin users: " + users.size());
    }

    @FXML
    private void handleCreateUser() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/user_create_form.fxml"));
            Parent form = loader.load();
            UserCreateController controller = (UserCreateController)loader.getController();

            Dialog<User> dialog = new Dialog<>();
            dialog.setResizable(true);// Account for window decorations
            dialog.getDialogPane().setPrefSize(530, 750);
            dialog.setTitle("Create New User");
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);


            dialog.setOnShown(e -> {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                try (InputStream iconStream = getClass().getResourceAsStream("/com/example/livecycle/images/logo.png")) {

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
                try {
                    if (userService.ajouter(user)) {
                        loadUsers(); // Refresh the list properly
                        userTable.refresh();
                        showAlert("Success", "User created successfully!");
                    }
                } catch (SQLException throwables) {
                    throw new RuntimeException(throwables);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/backoffice/user_edit_form.fxml"));
                Parent form = loader.load();

                // Get the form controller and pass in the selected user's data
                UserFormController formController = (UserFormController)loader.getController();
                formController.setUser(user);  // Pre-fill form fields including role
;
                // Create a dialog to display the form
                Dialog<User> dialog = new Dialog<>();
                dialog.setResizable(true);
                dialog.getDialogPane().setPrefSize(400, 750);
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
                dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/logo.png")));

                // Show the dialog and update the user if the result is present
                Optional<User> result = dialog.showAndWait();
                result.ifPresent(updatedUser -> {
                    try {
                        if (userService.modifier(updatedUser)) {
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
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
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

    private void handleDeleteUser(User user) throws SQLException {
        if (user != null) {
            if (userService.supprimer(user.getId())) {
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


    @FXML
    private void handleBanUser(User user) {
        try {
            if (userService.banUser(user.getId())) {
                // re-fetch so isBanned() really comes back as true
                loadUsers();
                userTable.refresh();
                showAlert("Success", "User has been banned");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to ban user");
        }
    }

    @FXML
    private void handleUnbanUser(User user) {
        try {
            if (userService.unbanUser(user.getId())) {
                // re-fetch so isBanned() really comes back as false
                loadUsers();
                userTable.refresh();
                showAlert("Success", "User has been unbanned");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to unban user");
        }
    }

}
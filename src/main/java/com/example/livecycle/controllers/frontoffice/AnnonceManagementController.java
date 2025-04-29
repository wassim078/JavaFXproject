package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Panier;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.PanierService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AnnonceManagementController {
    @FXML private GridPane annoncesGrid;
    @FXML private StackPane rootStackPane;


    private User currentUser;
    private final AnnonceService annonceService = new AnnonceService();
    private UserDashboardController dashboardController;

    private final PanierService panierService = new PanierService();



    public void setDashboardController(UserDashboardController dash) {
        this.dashboardController = dash;
    }

    public void initialize() {}

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAllAnnonces();
    }

    private void loadUserAnnonces() {
        try {
            List<Annonce> annonces = annonceService.getByUserId(currentUser.getId());
            populateGrid(annonces);
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }

    private void loadAllAnnonces() {
        try {
            List<Annonce> annonces = annonceService.recuperer();
            populateGrid(annonces);
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }

    private void populateGrid(List<Annonce> annonces) {
        annoncesGrid.getChildren().clear();
        for (int i = 0; i < annonces.size(); i++) {
            addAnnonceCard(annonces.get(i), i);
        }
    }


    private void addAnnonceCard(Annonce annonce, int index) {
        // Main card container
        VBox card = new VBox();
        card.getStyleClass().add("annonce-card");
        card.setSpacing(10);

        // Image Section
        ImageView imageView = createImageView(annonce);
        VBox imageContainer = new VBox(imageView);
        imageContainer.getStyleClass().add("annonce-image-container");

        // Details Section
        VBox details = createDetailsSection(annonce);

        // Cart Icon Container
        StackPane cartContainer = createCartIcon(annonce);
        VBox.setMargin(cartContainer, new Insets(10, 0, 0, 20)); // Left margin

        // Add all elements to card
        card.getChildren().addAll(
                imageContainer,
                details,
                cartContainer
        );

        // Only show cart for enterprise users
        cartContainer.setVisible(currentUser != null &&
                currentUser.getRoles().contains("ROLE_ENTREPRISE"));

        // Add to grid
        int column = index % 3;
        int row = index / 3;
        annoncesGrid.add(card, column, row);
    }





    private ImageView createImageView(Annonce annonce) {
        ImageView imageView = new ImageView();
        try {
            if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
                File file = new File(annonce.getImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(),
                            320,  // requested width
                            200,  // requested height
                            true, // preserve ratio
                            true, // smooth filtering
                            true  // load in background
                    );
                    imageView.setImage(image);
                }
            }
        } catch (Exception e) {
            Image defaultImage = new Image(
                    getClass().getResourceAsStream("/images/default-annonce.png"),
                    320, 200, true, true
            );
            imageView.setImage(defaultImage);
        }

        // Force exact dimensions
        imageView.setFitWidth(320);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false); // Force exact dimensions

        imageView.getStyleClass().add("annonce-image");
        return imageView;
    }

    private VBox createDetailsSection(Annonce annonce) {
        VBox details = new VBox(10);
        details.getStyleClass().add("detail-section");

        Text title = new Text(annonce.getTitre());
        title.getStyleClass().add("annonce-title");

        VBox priceSection = new VBox(5);
        priceSection.getStyleClass().add("price-section");

        Text price = new Text(String.format("%,.2f TND", annonce.getPrix()));
        price.getStyleClass().add("price-text");

        Label quantity = new Label("Available: " + annonce.getQuantite() + " units");
        quantity.getStyleClass().add("quantity-label");

        priceSection.getChildren().addAll(price, quantity);
        details.getChildren().addAll(title, priceSection);

        return details;
    }

    private StackPane createCartIcon(Annonce annonce) {
        ImageView cartIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/com/example/livecycle/images/cart-icon.png"))
        );
        cartIcon.setFitWidth(35);
        cartIcon.setFitHeight(20);

        StackPane cartContainer = new StackPane(cartIcon);
        cartContainer.getStyleClass().add("cart-icon");
        StackPane.setAlignment(cartContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(cartContainer, new Insets(0, 0, 10, 15));


        cartContainer.setOnMouseEntered(e -> {
            cartContainer.setStyle("-fx-background-color: #45a049;");
        });

        cartContainer.setOnMouseExited(e -> {
            cartContainer.setStyle("-fx-background-color: #4CAF50;");
        });


        boolean isEnterprise = currentUser != null &&
                currentUser.getRoles().contains("ROLE_ENTREPRISE");
        cartContainer.setVisible(isEnterprise);

        if (isEnterprise) {
            cartContainer.setOnMouseClicked(e -> handleAddToCart(annonce));
        }
        return cartContainer;
    }

    private void handleAddToCart(Annonce annonce) {
        try {
            // Get fresh annonce data from database
            Annonce currentAnnonce = annonceService.getById(annonce.getId());
            if (currentAnnonce == null) {
                showError("Product not found!");
                return;
            }

            // Get current cart quantity
            int cartQty = getCurrentCartQuantity(currentAnnonce.getId());
            int availableQty = currentAnnonce.getQuantite();

            if (cartQty >= availableQty) {
                showErrorMessage("Maximum quantity reached! (" + availableQty + " available)");
                return;
            }

            if ((cartQty + 1) > availableQty) {
                showErrorMessage("Cannot add more than " + (availableQty - cartQty) + " units");
                return;
            }

            panierService.addItem(currentUser.getId(), currentAnnonce.getId(), 1);
            dashboardController.refreshCartCount();
            showSuccessMessage();
        } catch (SQLException e) {
            showError("Could not add to cart: " + e.getMessage()); // Keep as dialog for system errors
        }
    }


    private int getCurrentCartQuantity(int annonceId) throws SQLException {
        Panier panier = panierService.getByUserId(currentUser.getId());
        if (panier == null || panier.getItems() == null) return 0;

        JSONObject items = new JSONObject(panier.getItems());
        return items.optInt(String.valueOf(annonceId), 0);
    }

    @FXML
    private void handleCreateAnnonce() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_annonce.fxml"));
            GridPane form = loader.load();
            CreateAnnonceController controller = loader.getController();

            if (currentUser == null) {
                showError("User not logged in!");
                return;
            }

            controller.setUser(currentUser);
            controller.setRefreshCallback(this::loadUserAnnonces);
            annoncesGrid.getChildren().clear();
            annoncesGrid.add(form, 0, 0);
        } catch (IOException e) {
            showError("Error loading form: " + e.getMessage());
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }
    private void showSuccessMessage() {
        Platform.runLater(() -> {
            // Check if rootStackPane is available
            if (rootStackPane == null) {
                System.err.println("rootStackPane not initialized!");
                return;
            }

            Label successLabel = new Label("Annonce added to cart!");
            successLabel.getStyleClass().add("success-message");

            StackPane overlay = new StackPane();
            overlay.setAlignment(Pos.CENTER);
            overlay.setStyle("-fx-background-color: transparent;");
            overlay.setPickOnBounds(false);
            StackPane.setMargin(successLabel, new Insets(50, 0, 0, 0));
            overlay.getChildren().add(successLabel);

            // Add overlay to the rootStackPane (the AnnonceManagement's StackPane)
            rootStackPane.getChildren().add(overlay);

            PauseTransition delay = new PauseTransition(Duration.seconds(0.7));
            delay.setOnFinished(e -> rootStackPane.getChildren().remove(overlay));
            delay.play();
        });
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            if (rootStackPane == null) {
                System.err.println("rootStackPane not initialized!");
                return;
            }

            Label errorLabel = new Label(message);
            errorLabel.getStyleClass().add("error-message");

            StackPane overlay = new StackPane();
            overlay.setAlignment(Pos.CENTER);
            overlay.setStyle("-fx-background-color: transparent;");
            overlay.setPickOnBounds(false);
            StackPane.setMargin(errorLabel, new Insets(50, 0, 0, 0));
            overlay.getChildren().add(errorLabel);

            rootStackPane.getChildren().add(overlay);

            PauseTransition delay = new PauseTransition(Duration.seconds(0.7));
            delay.setOnFinished(e -> rootStackPane.getChildren().remove(overlay));
            delay.play();
        });
    }


}
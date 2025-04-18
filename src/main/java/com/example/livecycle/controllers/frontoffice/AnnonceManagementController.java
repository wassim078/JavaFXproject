package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CommandeService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AnnonceManagementController {
    @FXML private GridPane annoncesGrid;

    private User currentUser;
    private final AnnonceService annonceService = new AnnonceService();
    private UserDashboardController dashboardController;


    public void setDashboardController(UserDashboardController dash) {
        this.dashboardController = dash;
    }

    public void initialize() {
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAllAnnonces();
    }
    private void loadUserAnnonces() {

        try {
            List<Annonce> annonces = annonceService.getByUserId(currentUser.getId());
            annoncesGrid.getChildren().clear();

            int row = 0;
            for (Annonce annonce : annonces) {
                addAnnonceCard(annonce, row);
                row++;
            }
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }
    private void loadAllAnnonces() {
        try {
            List<Annonce> annonces = annonceService.recuperer(); // Get ALL
            annoncesGrid.getChildren().clear();
            int row = 0;
            for (Annonce annonce : annonces) {
                addAnnonceCard(annonce, row);
                row++;
            }
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }


    private void addAnnonceCard(Annonce annonce, int row) {
        VBox card = new VBox();
        card.getStyleClass().add("annonce-card");

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);

        // Image Container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("annonce-image-container");
        ImageView imageView = new ImageView();
        try {
            if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
                File file = new File(annonce.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-annonce.png")));
        }

        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("annonce-image");
        imageContainer.getChildren().add(imageView);

        // Details Section
        VBox details = new VBox();
        details.getStyleClass().add("detail-section");

        Text title = new Text(annonce.getTitre());
        title.getStyleClass().add("title-text");

        Label category = new Label(annonce.getCategorieAnnonce().getName());
        category.getStyleClass().add("category-tag");

        Text description = new Text(annonce.getDescription());
        description.getStyleClass().add("detail-label");
        description.setWrappingWidth(400);

        VBox priceSection = new VBox();
        priceSection.getStyleClass().add("price-section");
        Text price = new Text(String.format("%,.2f TND", annonce.getPrix()));
        price.getStyleClass().add("price-text");
        Label quantity = new Label("Available: " + annonce.getQuantite() + " units");
        quantity.getStyleClass().add("quantity-label");

        priceSection.getChildren().addAll(price, quantity);

        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("add-to-cart-btn");


        // Show button only for enterprise users
        boolean isEnterprise = currentUser != null &&
                currentUser.getRoles().contains("ROLE_ENTREPRISE");
        addToCartBtn.setVisible(isEnterprise);

        // Only add action handler if visible
        if (isEnterprise) {
            addToCartBtn.setOnAction(e -> handleAddToCart(annonce));
        }

        buttonContainer.getChildren().add(addToCartBtn);
        details.getChildren().add(buttonContainer);

        details.getChildren().addAll(title, category, description, priceSection);
        details.setSpacing(15);

        content.getChildren().addAll(imageContainer, details);
        content.setSpacing(20);

        card.getChildren().add(content);
        annoncesGrid.add(card, 0, row);

    }


    private void handleAddToCart(Annonce annonce) {
        try {
            // 1) persist to your commande table (adds or updates the JSON quantity)
            new CommandeService()
                    .addOrUpdateCommande(currentUser.getId(),
                            annonce.getId(),
                            1);

            // 2) update the red badge in the dashboard
            if (dashboardController != null) {
                dashboardController.incrementCartCount();
            }

        } catch (SQLException e) {
            showError("Could not add to cart: " + e.getMessage());
        }
    }




    private void handleBuyAnnouncement(Annonce annonce) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/purchase.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) annoncesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showError("Error loading purchase page: " + e.getMessage());
        }
    }


    private Text createDetailText(String content) {
        Text text = new Text(content);
        text.setWrappingWidth(300);
        return text;
    }


    @FXML
    private void handleCreateAnnonce() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_annonce.fxml"));
            GridPane form = loader.load();

            CreateAnnonceController controller = loader.getController();

            // Add null check and error handling
            if (currentUser == null) {
                showError("User not logged in!");
                return;
            }

            controller.setUser(currentUser); // Pass the current user
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



}
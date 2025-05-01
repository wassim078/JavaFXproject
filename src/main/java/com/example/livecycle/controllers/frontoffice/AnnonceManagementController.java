package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.entities.Panier;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CategoryAnnonceService;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.PanierService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;
import javafx.scene.control.ButtonBar;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class AnnonceManagementController implements Initializable {
    @FXML private GridPane annoncesGrid;
    @FXML private StackPane rootStackPane;
    @FXML private Pagination pagination;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> weightFilter;
    @FXML private Button resetFilters;

    private static final int ITEMS_PER_PAGE = 3;
    private User currentUser;
    private final AnnonceService annonceService = new AnnonceService();
    private UserDashboardController dashboardController;
    private final PanierService panierService = new PanierService();
    private List<Annonce> allAnnonces = new ArrayList<>();
    private final CategoryAnnonceService categoryService = new CategoryAnnonceService();

    public void setDashboardController(UserDashboardController dash) {
        this.dashboardController = dash;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        setupSearch();
        setupPagination();
    }

    private void setupPagination() {
        try {
            int totalItems = annonceService.getTotalAnnoncesCount();
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            
            pagination.setPageCount(Math.max(1, totalPages));
            pagination.setCurrentPageIndex(0);
            pagination.setMaxPageIndicatorCount(5);
            
            // Ajouter un listener pour le changement de page
            pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                loadPage(newIndex.intValue());
            });
            
            // Charger la première page
            loadPage(0);
        } catch (SQLException e) {
            showError("Error setting up pagination: " + e.getMessage());
        }
    }

    private void loadPage(int pageIndex) {
        try {
            List<Annonce> annonces;
            if (currentUser != null) {
                annonces = annonceService.recupererAvecFavoris(currentUser.getId());
            } else {
                annonces = annonceService.recuperer();
            }
            
            // Appliquer les filtres
            annonces = applyFilters(annonces);
            
            int fromIndex = pageIndex * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, annonces.size());
            
            if (fromIndex >= annonces.size()) {
                populateGrid(Collections.emptyList());
                return;
            }
            
            List<Annonce> pageAnnonces = annonces.subList(fromIndex, toIndex);
            populateGrid(pageAnnonces);
            
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupPagination();
    }

    private void loadUserAnnonces() {
        try {
            List<Annonce> annonces = annonceService.getByUserId(currentUser.getId());
            populateGrid(annonces);
        } catch (SQLException e) {
            showError("Error loading announcements: " + e.getMessage());
        }
    }

    private void populateGrid(List<Annonce> annonces) {
        annoncesGrid.getChildren().clear();
        annoncesGrid.getColumnConstraints().clear();
        annoncesGrid.getRowConstraints().clear();

        // Configuration des colonnes
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHalignment(javafx.geometry.HPos.CENTER);
            annoncesGrid.getColumnConstraints().add(col);
        }

        // Ajout des annonces
        for (int i = 0; i < annonces.size(); i++) {
            int column = i % 3;
            int row = i / 3;
            VBox card = createAnnonceCard(annonces.get(i));
            annoncesGrid.add(card, column, row);
            GridPane.setHalignment(card, javafx.geometry.HPos.CENTER);
        }
    }

    private VBox createAnnonceCard(Annonce annonce) {
        VBox card = new VBox(8);
        card.getStyleClass().add("annonce-card");
        card.setMaxWidth(180);  // Reduced from 200
        card.setMinWidth(180);  // Reduced from 200
        card.setAlignment(Pos.TOP_CENTER);  // Changed alignment

        // Top bar with buttons
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(0, 5, 5, 0));

        // Cart icon first (left side)
        StackPane cartIcon = createCartIcon(annonce);
        HBox.setMargin(cartIcon, new Insets(0, 5, 0, 0));

        // Favorite button
        Button favoriteButton = new Button();
        favoriteButton.getStyleClass().add("favorite-button");
        if (annonce.isFavori()) {
            favoriteButton.getStyleClass().add("active");
        }

        Text starIcon = new Text(annonce.isFavori() ? "★" : "☆");
        starIcon.getStyleClass().add("icon");
        favoriteButton.setGraphic(starIcon);

        // Add buttons to top bar
        topBar.getChildren().addAll(cartIcon, favoriteButton);

        // Image
        ImageView imageView = createImageView(annonce);
        VBox imageContainer = new VBox(imageView);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(5));

        // Details
        VBox details = new VBox(5);
        details.getStyleClass().add("detail-section");
        details.setAlignment(Pos.CENTER);

        Text title = new Text(annonce.getTitre());
        title.getStyleClass().add("annonce-title");

        Label category = new Label(annonce.getCategorieAnnonce().getName());
        category.getStyleClass().add("category-badge");

        Text price = new Text(String.format("%.2f TND", annonce.getPrix()));
        price.getStyleClass().add("price-text");

        Text descriptionPreview = new Text(getPreviewText(annonce.getDescription(), 60));  // Reduced preview length
        descriptionPreview.getStyleClass().add("description-preview");

        details.getChildren().addAll(title, category, price, descriptionPreview);

        // Add all elements to card
        card.getChildren().addAll(topBar, imageContainer, details);

        // Event handlers
        card.setOnMouseClicked(e -> {
            // Only show details if the click is not on the cart icon
            if (!(e.getTarget() instanceof StackPane && ((Node)e.getTarget()).getStyleClass().contains("cart-icon"))) {
                showAnnonceDetails(annonce);
            }
        });
        favoriteButton.setOnAction(e -> handleFavoriteToggle(annonce, starIcon, favoriteButton));

        return card;
    }

    private String getPreviewText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private void showAnnonceDetails(Annonce annonce) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Announcement Details");
        
        VBox content = new VBox(20);
        content.getStyleClass().add("detail-dialog");
        content.setPrefWidth(500);
        
        // Header with image
        ImageView imageView = createImageView(annonce);
        imageView.setFitWidth(450);
        imageView.setFitHeight(250);
        
        // Title section
        Text title = new Text(annonce.getTitre());
        title.getStyleClass().add("detail-dialog-title");
        
        // Category badge
        Label category = new Label(annonce.getCategorieAnnonce().getName());
        category.getStyleClass().add("category-badge");
        
        // Main details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.getStyleClass().add("detail-section");
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        
        // Add details with icons
        addDetailRow(detailsGrid, 0, "💰 Price", String.format("%.2f TND", annonce.getPrix()));
        addDetailRow(detailsGrid, 1, "📦 Quantity", String.valueOf(annonce.getQuantite()));
        addDetailRow(detailsGrid, 2, "⚖️ Weight", String.format("%.2f kg", annonce.getPoids()));
        
        // Description section
        VBox descriptionBox = new VBox(10);
        descriptionBox.getStyleClass().add("detail-section");
        Label descLabel = new Label("📝 Description");
        descLabel.getStyleClass().add("detail-label");
        Text descValue = new Text(annonce.getDescription());
        descValue.getStyleClass().add("detail-value");
        descValue.setWrappingWidth(430);
        descriptionBox.getChildren().addAll(descLabel, descValue);
        
        // Add all components
        content.getChildren().addAll(
            imageView,
            title,
            category,
            detailsGrid,
            descriptionBox
        );
        
        // Dialog setup
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/example/livecycle/css/shop_annonce.css").toExternalForm()
        );
        
        // Show dialog
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("detail-label");
        
        Text valueNode = new Text(value);
        valueNode.getStyleClass().add("detail-value");
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private ImageView createImageView(Annonce annonce) {
        ImageView imageView = new ImageView();
        try {
            if (annonce.getImage() != null && !annonce.getImage().isEmpty()) {
                File file = new File(annonce.getImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(),
                            160,  // Reduced width
                            100,  // Reduced height
                            true, true, true);
                    imageView.setImage(image);
                }
            }
        } catch (Exception e) {
            Image defaultImage = new Image(
                    getClass().getResourceAsStream("/images/default-annonce.png"),
                    160, 100, true, true);
            imageView.setImage(defaultImage);
        }

        imageView.setFitWidth(160);  // Reduced from 320
        imageView.setFitHeight(100); // Reduced from 200
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("annonce-image");
        return imageView;
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
            cartContainer.setOnMouseClicked(e -> {
                handleAddToCart(annonce);
                e.consume(); // Prevent event propagation
            });
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

    private void setupFilters() {
        // Setup category filter
        try {
            List<Category> categories = categoryService.recuperer();
            List<String> categoryNames = categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            categoryFilter.getItems().add("All Categories");
            categoryFilter.getItems().addAll(categoryNames);
            categoryFilter.setValue("All Categories");
        } catch (SQLException e) {
            showError("Error loading categories: " + e.getMessage());
        }

        // Setup price ranges
        priceFilter.getItems().addAll(
            "All Prices",
            "Under 50 TND",
            "50-100 TND",
            "100-200 TND",
            "Over 200 TND"
        );
        priceFilter.setValue("All Prices");

        // Setup weight ranges
        weightFilter.getItems().addAll(
            "All Weights",
            "Under 1 kg",
            "1-5 kg",
            "5-10 kg",
            "Over 10 kg"
        );
        weightFilter.setValue("All Weights");

        // Add listeners
        categoryFilter.setOnAction(e -> applyFilters());
        priceFilter.setOnAction(e -> applyFilters());
        weightFilter.setOnAction(e -> applyFilters());
        resetFilters.setOnAction(e -> resetAllFilters());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2 || newValue.isEmpty()) {
                applyFilters();
            }
        });
    }

    private void resetAllFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        priceFilter.setValue("All Prices");
        weightFilter.setValue("All Weights");
        applyFilters();
    }

    private List<Annonce> applyFilters(List<Annonce> annonces) {
        List<Annonce> filteredAnnonces = new ArrayList<>(annonces);
        
        // Apply search filter
        String searchText = searchField.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredAnnonces = filteredAnnonces.stream()
                .filter(a -> a.getTitre().toLowerCase().contains(searchText) ||
                           a.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }

        // Apply category filter
        String selectedCategory = categoryFilter.getValue();
        if (!"All Categories".equals(selectedCategory)) {
            filteredAnnonces = filteredAnnonces.stream()
                .filter(a -> a.getCategorieAnnonce().getName().equals(selectedCategory))
                .collect(Collectors.toList());
        }

        // Apply price filter
        String selectedPrice = priceFilter.getValue();
        filteredAnnonces = filterByPrice(filteredAnnonces, selectedPrice);

        // Apply weight filter
        String selectedWeight = weightFilter.getValue();
        filteredAnnonces = filterByWeight(filteredAnnonces, selectedWeight);

        return filteredAnnonces;
    }

    private void applyFilters() {
        loadPage(0);
    }

    private List<Annonce> filterByPrice(List<Annonce> annonces, String priceRange) {
        if ("All Prices".equals(priceRange)) return annonces;
        
        return annonces.stream().filter(a -> {
            double price = a.getPrix();
            switch (priceRange) {
                case "Under 50 TND": return price < 50;
                case "50-100 TND": return price >= 50 && price <= 100;
                case "100-200 TND": return price > 100 && price <= 200;
                case "Over 200 TND": return price > 200;
                default: return true;
            }
        }).collect(Collectors.toList());
    }

    private List<Annonce> filterByWeight(List<Annonce> annonces, String weightRange) {
        if ("All Weights".equals(weightRange)) return annonces;
        
        return annonces.stream().filter(a -> {
            double weight = a.getPoids();
            switch (weightRange) {
                case "Under 1 kg": return weight < 1;
                case "1-5 kg": return weight >= 1 && weight <= 5;
                case "5-10 kg": return weight > 5 && weight <= 10;
                case "Over 10 kg": return weight > 10;
                default: return true;
            }
        }).collect(Collectors.toList());
    }

    private void displayFilteredResults(List<Annonce> annonces, int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, annonces.size());
        
        if (fromIndex >= annonces.size()) {
            populateGrid(Collections.emptyList());
            return;
        }
        
        List<Annonce> pageAnnonces = annonces.subList(fromIndex, toIndex);
        populateGrid(pageAnnonces);
    }




    private void handleFavoriteToggle(Annonce annonce, Text starIcon, Button favoriteButton) {
        try {
            boolean success = annonceService.toggleFavori(currentUser.getId(), annonce.getId());
            if (success) {
                annonce.setFavori(!annonce.isFavori());
                starIcon.setText(annonce.isFavori() ? "★" : "☆");
                favoriteButton.getStyleClass().removeAll("active");
                if (annonce.isFavori()) {
                    favoriteButton.getStyleClass().add("active");
                }
                loadPage(pagination.getCurrentPageIndex());
            }
        } catch (SQLException ex) {
            showError("Error updating favorite status: " + ex.getMessage());
        }
    }
}
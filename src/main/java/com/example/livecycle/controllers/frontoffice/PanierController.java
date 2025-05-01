package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Panier;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.PanierService;
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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PanierController {
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;

    private User currentUser;
    private UserDashboardController dashboardController;
    private final AnnonceService annonceService = new AnnonceService();
    private final PanierService panierService = new PanierService();

    public void initData(User user, UserDashboardController dash) {
        this.currentUser = user;
        this.dashboardController = dash;
        loadPanierDetails();
    }

    private void loadPanierDetails() {
        itemsContainer.getChildren().clear();
        double total = 0;

        try {
            Panier panier = panierService.getByUserId(currentUser.getId());
            if (panier == null || panier.getItems() == null || panier.getItems().isEmpty()) {
                totalLabel.setText("0.00 TND");
                return;
            }

            JSONObject items = new JSONObject(panier.getItems());

            for (String key : items.keySet()) {
                try {
                    int annonceId = Integer.parseInt(key);
                    Object qtyValue = items.get(key);
                    int qty = parseQuantity(qtyValue);

                    Annonce annonce = annonceService.getById(annonceId);
                    if (annonce != null) {
                        HBox itemRow = createCartItemRow(annonce, qty);
                        itemsContainer.getChildren().add(itemRow);
                        total += annonce.getPrix() * qty;
                    }
                } catch (Exception e) {
                    System.err.println("Error processing item: " + key);
                    e.printStackTrace();
                }
            }

            totalLabel.setText(String.format("%.2f TND", total));
        } catch (Exception ex) {
            showError("Error loading cart: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private HBox createCartItemRow(Annonce annonce, int initialQty) {
        // 1) Image
        ImageView thumb = new ImageView();
        File imgFile = new File(annonce.getImage());
        if (imgFile.exists()) {
            thumb.setImage(new Image(imgFile.toURI().toString()));
        }
        thumb.setFitWidth(80);
        thumb.setFitHeight(80);
        thumb.setPreserveRatio(true);

        // 2) Info and Error Label
        Label title = new Label(annonce.getTitre());
        Label price = new Label(String.format("%.2f TND each", annonce.getPrix()));
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        VBox infoBox = new VBox(4, title, price, errorLabel);

        // 3) Quantity Controls
        Button minus = new Button("–");
        Button plus = new Button("+");
        Label qtyLabel = new Label(String.valueOf(initialQty));

        plus.setOnAction(e -> {
            int currentQty = Integer.parseInt(qtyLabel.getText());
            int newQty = currentQty + 1;

            try {
                Annonce currentAnnonce = annonceService.getById(annonce.getId());
                if (currentAnnonce == null) {
                    showError("Product not available!");
                    return;
                }

                if (newQty > currentAnnonce.getQuantite()) {
                    errorLabel.setText("Max " + currentAnnonce.getQuantite() + " units available");
                    errorLabel.setVisible(true);
                } else {
                    errorLabel.setVisible(false);
                    updateQuantity(annonce.getId(), newQty);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Error updating quantity");
            }
        });

        minus.setOnAction(e -> {
            int currentQty = Integer.parseInt(qtyLabel.getText());
            updateQuantity(annonce.getId(), currentQty - 1);
            errorLabel.setVisible(false); // Clear error when decreasing
        });

        HBox qtyBox = new HBox(5, minus, qtyLabel, plus);
        qtyBox.setAlignment(Pos.CENTER);

        // 4) Subtotal
        Label subtotal = new Label(String.format("%.2f TND", annonce.getPrix() * initialQty));

        // Final assembly
        HBox row = new HBox(15, thumb, infoBox, new Region(), qtyBox, subtotal);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);

        return row;
    }

    private void updateQuantity(int annonceId, int newQty) {
        try {
            Panier panier = panierService.getByUserId(currentUser.getId());
            JSONObject items = panier != null ?
                    new JSONObject(panier.getItems()) : new JSONObject();

            if (newQty <= 0) {
                items.remove(String.valueOf(annonceId));
            } else {
                // Additional safety check
                Annonce annonce = annonceService.getById(annonceId);
                if (annonce != null && newQty > annonce.getQuantite()) {
                    return; // Should never happen due to UI check
                }
                items.put(String.valueOf(annonceId), newQty);
            }

            panierService.updatePanierItems(currentUser.getId(), items.toString());
            dashboardController.refreshCartCount();
            loadPanierDetails(); // This will recreate rows with fresh data

        } catch (SQLException ex) {
            showError("Error updating quantity");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            Panier panier = panierService.getByUserId(currentUser.getId());
            if (panier == null || panier.getItems().isEmpty()) {
                showError("Your cart is empty!");
                return;
            }

            // Create new stage for checkout instead of using main window's stage
            Stage checkoutStage = new Stage();
            checkoutStage.initOwner(itemsContainer.getScene().getWindow());
            checkoutStage.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/checkout_form.fxml"));
            Parent root = loader.load();

            CheckoutController controller = loader.getController();
            controller.initData(
                    currentUser,
                    panier,
                    calculateTotal(panier),
                    checkoutStage,  // Pass the new checkout stage
                    dashboardController
            );

            checkoutStage.setScene(new Scene(root));
            checkoutStage.setTitle("Checkout");
            checkoutStage.showAndWait();

            loadPanierDetails();
            dashboardController.refreshCartCount();

        } catch (IOException | SQLException e) {
            showError("Checkout error: " + e.getMessage());
        }
    }

    private double calculateTotal(Panier panier) throws SQLException {
        JSONObject items = new JSONObject(panier.getItems());
        double total = 0;

        for (String key : items.keySet()) {
            int annonceId = Integer.parseInt(key);
            int qty = items.getInt(key);
            Annonce annonce = annonceService.getById(annonceId);
            total += annonce.getPrix() * qty;
        }
        return total;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private int parseQuantity(Object quantityValue) {
        if (quantityValue instanceof Integer) return (Integer) quantityValue;
        if (quantityValue instanceof JSONArray) {
            return ((JSONArray) quantityValue).optInt(0, 1);
        }
        try {
            return Integer.parseInt(quantityValue.toString());
        } catch (NumberFormatException e) {
            return 1;
        }
    }





}
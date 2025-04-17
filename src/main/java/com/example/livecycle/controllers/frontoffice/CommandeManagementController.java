package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Commande;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.AnnonceService;
import com.example.livecycle.services.CommandeService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class CommandeManagementController {
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;

    private User currentUser;
    private UserDashboardController dashboardController;

    private final CommandeService commandeService = new CommandeService();
    private final AnnonceService annonceService = new AnnonceService();

    public void initData(User user, UserDashboardController dash) {
        this.currentUser = user;
        this.dashboardController = dash;
        loadCommandeDetails();
    }

    private void loadCommandeDetails() {
        itemsContainer.getChildren().clear();
        double total = 0;

        try {
            List<Commande> commandes = commandeService.getByUserId(currentUser.getId());
            if (commandes.isEmpty()) {
                totalLabel.setText("0.00 TND");
                return;
            }

            Commande cart = commandes.get(0);
            JSONObject obj = new JSONObject(cart.getAnnonceQuantities());

            for (String key : obj.keySet()) {
                int annonceId = Integer.parseInt(key);
                int qty = obj.getInt(key);
                Annonce annonce = annonceService.getById(annonceId);

                // 1) Thumbnail
                ImageView thumb = new ImageView();
                File imgFile = new File(annonce.getImage());
                if (imgFile.exists()) {
                    thumb.setImage(new Image(imgFile.toURI().toString()));
                }
                thumb.setFitWidth(80);
                thumb.setFitHeight(80);
                thumb.setPreserveRatio(true);
                thumb.getStyleClass().add("item-thumb");

                // 2) Info (title + unit price)
                Label title = new Label(annonce.getTitre());
                title.getStyleClass().add("item-title");
                Label unitPrice = new Label(String.format("%.2f TND each", annonce.getPrix()));
                unitPrice.getStyleClass().add("item-unit");
                VBox infoBox = new VBox(4, title, unitPrice);
                infoBox.getStyleClass().add("item-info");

                // 3) Spacer pushes qty & subtotal to the right
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // 4) Qty controls
                Button minus = new Button("–");
                Button plus  = new Button("+");
                Label qtyLabel = new Label(String.valueOf(qty));
                minus.getStyleClass().addAll("qty-btn", "qty-minus");
                plus.getStyleClass().addAll("qty-btn", "qty-plus");
                qtyLabel.getStyleClass().add("qty-label");
                HBox qtyBox = new HBox(6, minus, qtyLabel, plus);
                qtyBox.setAlignment(Pos.CENTER);
                qtyBox.getStyleClass().add("item-qty");

                // 5) Subtotal
                Label subtotal = new Label(String.format("%.2f TND", annonce.getPrix() * qty));
                subtotal.getStyleClass().add("item-subtotal");

                // 6) Assemble row
                HBox row = new HBox(12, thumb, infoBox, spacer, qtyBox, subtotal);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("item-row");

                // 7) Handlers
                int finalQty = qty;
                minus.setOnAction(e -> updateQuantity(annonceId, finalQty - 1));
                plus.setOnAction(e -> updateQuantity(annonceId, finalQty + 1));

                itemsContainer.getChildren().add(row);
                total += annonce.getPrix() * qty;
            }

            totalLabel.setText(String.format("%.2f TND", total));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateQuantity(int annonceId, int newQty) {
        try {
            // 1) Fetch the current cart
            List<Commande> commandes = commandeService.getByUserId(currentUser.getId());
            if (commandes.isEmpty()) return;

            Commande cart = commandes.get(0);
            JSONObject obj = new JSONObject(cart.getAnnonceQuantities());

            // 2) Update or remove quantity
            if (newQty <= 0) {
                obj.remove(String.valueOf(annonceId)); // Remove item
            } else {
                obj.put(String.valueOf(annonceId), newQty); // Update quantity
            }

            // 3) Save to database
            cart.setAnnonceQuantities(obj.toString());
            commandeService.modifier(cart);

            // 4) Update UI
            dashboardController.loadCartCount();
            loadCommandeDetails();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleCheckout() {
        // TODO: implement purchase flow
        System.out.println("▶ Checkout clicked!");
    }
}

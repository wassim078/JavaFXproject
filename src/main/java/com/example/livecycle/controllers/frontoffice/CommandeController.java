package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.AnnonceService;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.json.JSONObject;
import com.example.livecycle.controllers.frontoffice.UserDashboardController;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommandeController {

    @FXML private ListView<Commande> commandesListView;

    private User currentUser;
    private final CommandeService commandeService = new CommandeService();
    private final AnnonceService annonceService = new AnnonceService();
    private UserDashboardController dashboardController;

    public void initialize() {
        setupListView();
        loadCommandes();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadCommandes();
    }


    public void setDashboardController(UserDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }


    private void setupListView() {
        commandesListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Commande> call(ListView<Commande> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Commande commande, boolean empty) {
                        super.updateItem(commande, empty);
                        if (empty || commande == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(createCommandeCard(commande));
                        }
                    }
                };
            }
        });
    }

    private VBox createCommandeCard(Commande commande) {
        VBox card = new VBox(10);
        card.getStyleClass().add("commande-card");

        // Header with date and status
        HBox header = new HBox(10);
        Label dateLabel = new Label(commande.getDate().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        Label statusLabel = new Label(commande.getEtatCommande());
        statusLabel.getStyleClass().add("status-" + commande.getEtatCommande().toLowerCase());

        header.getChildren().addAll(dateLabel, statusLabel);

        // Items list
        VBox itemsBox = new VBox(5);
        JSONObject items = new JSONObject(commande.getAnnonceQuantities());
        items.keySet().forEach(annonceId -> {
            try {
                int id = Integer.parseInt(annonceId);
                int quantity = items.getInt(annonceId);
                String itemName = annonceService.getById(id).getTitre();
                itemsBox.getChildren().add(new Label(quantity + " x " + itemName));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // Footer with total and delete button
        HBox footer = new HBox(10);
        Label totalLabel = new Label(String.format("Total: %.2f TND", commande.getPrixTotal()));
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> handleDelete(commande));

        footer.getChildren().addAll(totalLabel, deleteBtn);

        card.getChildren().addAll(header, itemsBox, footer);
        return card;
    }

    private void handleDelete(Commande commande) {
        try {
            commandeService.supprimer(commande.getId());
            loadCommandes(); // Refresh the list
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCommandes() {
        if (currentUser == null) return;

        try {
            List<Commande> commandes = commandeService.getByUserId(currentUser.getId());
            commandesListView.getItems().setAll(commandes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
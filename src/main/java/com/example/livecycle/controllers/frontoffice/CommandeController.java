    package com.example.livecycle.controllers.frontoffice;

    import com.example.livecycle.entities.Commande;
    import com.example.livecycle.entities.User;
    import com.example.livecycle.services.CommandeService;
    import com.example.livecycle.services.AnnonceService;
    import javafx.fxml.FXML;
    import javafx.geometry.Pos;
    import javafx.scene.control.ListView;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.control.ListCell;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.StackPane;
    import javafx.scene.layout.VBox;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Circle;
    import javafx.scene.shape.Line;
    import javafx.util.Callback;
    import org.json.JSONObject;
    import com.example.livecycle.controllers.frontoffice.UserDashboardController;

    import java.sql.SQLException;
    import java.time.format.DateTimeFormatter;
    import java.util.Arrays;
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
            commandesListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Commande commande, boolean empty) {
                    super.updateItem(commande, empty);
                    if (empty || commande == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox card = createCommandeCard(commande);
                        card.setPrefWidth(commandesListView.getWidth() - 20);
                        setGraphic(card);
                    }
                }
            });
        }

        private VBox createCommandeCard(Commande commande) {
            // root card container
            VBox card = new VBox(10);
            card.getStyleClass().add("commande-card");
            card.setFillWidth(true);

            // 1) DATE LABEL
            Label dateLabel = new Label(
                    commande.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
            );
            dateLabel.getStyleClass().add("header-label");

            // 2) STATUS TIMELINE
            HBox statusTimeline = createStatusTimeline(commande);

            // put date + timeline in one row
            HBox headerWithTimeline = new HBox(20, dateLabel, statusTimeline);
            headerWithTimeline.setAlignment(Pos.CENTER_LEFT);

            // 3) ITEMS LIST
            VBox itemsBox = new VBox(5);
            itemsBox.setAlignment(Pos.TOP_LEFT);
            JSONObject itemsJson = new JSONObject(commande.getAnnonceQuantities());
            itemsJson.keySet().forEach(key -> {
                try {
                    int id       = Integer.parseInt(key);
                    int quantity = itemsJson.getInt(key);
                    String title = annonceService.getById(id).getTitre();
                    Label item = new Label(quantity + " x " + title);
                    itemsBox.getChildren().add(item);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            // 4) FOOTER (TOTAL + DELETE)
            Label totalLabel = new Label(String.format("Total: %.2f TND", commande.getPrixTotal()));
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("delete-button");
            deleteBtn.setOnAction(e -> handleDelete(commande));

            HBox footer = new HBox(15, totalLabel, deleteBtn);
            footer.setAlignment(Pos.CENTER_LEFT);

            // assemble card
            card.getChildren().addAll(
                    headerWithTimeline,
                    itemsBox,
                    footer
            );
            return card;
        }

        private HBox createStatusTimeline(Commande commande) {
            HBox timeline = new HBox(8);
            timeline.getStyleClass().add("status-timeline");
            timeline.setAlignment(Pos.CENTER_LEFT);

            // choose the right progression
            List<String> progression = commande.getMethodePaiement().equals("à la livraison")
                    ? List.of("AWAITING_DELIVERY", "PROCESSING", "SHIPPED", "DELIVERED")
                    : List.of("PAID", "PROCESSING", "SHIPPED", "DELIVERED");

            String current = commande.getEtatCommande();
            boolean cancelled = "CANCELLED".equals(current);

            for (int i = 0; i < progression.size(); i++) {
                String status = progression.get(i);

                // 1) load & force-resize the icon
                String iconName = status.equals("AWAITING_DELIVERY")
                        ? "awaiting_delivery"
                        : status.toLowerCase();
                ImageView rawIcon = new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/" + iconName + ".png"))
                );
                rawIcon.setFitWidth(24);
                rawIcon.setFitHeight(24);
                rawIcon.setPreserveRatio(true);

                // 2) draw a circle behind it
                Circle circle = new Circle(16);           // radius = 16px
                circle.setFill(Color.WHITE);
                circle.setStroke(Color.web("#f00020"));   // default border
                circle.setStrokeWidth(2);

                // color completed/current
                if (status.equals(current) || progression.indexOf(status) < progression.indexOf(current)) {
                    circle.setStroke(Color.web("#2ecc71"));
                }
                // fade if cancelled
                if (cancelled) {
                    rawIcon.setOpacity(0.4);
                    circle.setOpacity(0.4);
                }

                // stack them
                StackPane wrapper = new StackPane(circle, rawIcon);
                wrapper.setPrefSize(32, 32);
                wrapper.setAlignment(Pos.CENTER);

                // 3) label under the circle
                VBox item = new VBox(4, wrapper, new Label(status));
                item.getStyleClass().add("status-item");
                item.setAlignment(Pos.CENTER);

                // mark CSS classes for further styling if desired
                if (status.equals(current))    item.getStyleClass().add("current");
                if (progression.indexOf(status) < progression.indexOf(current)) item.getStyleClass().add("completed");
                if (cancelled)                 item.getStyleClass().add("cancelled");

                timeline.getChildren().add(item);

                // 4) connector line (except after last)
                if (i < progression.size() - 1) {
                    Line connector = new Line(0, 0, 20, 0);
                    connector.getStyleClass().add("status-line");
                    timeline.getChildren().add(connector);
                }
            }

            // if the whole order is cancelled, append a final CANCELLED bubble
            if (cancelled) {
                ImageView xIcon = new ImageView(
                        new Image(getClass().getResourceAsStream("/icons/cancelled.png"))
                );
                xIcon.setFitWidth(24);
                xIcon.setFitHeight(24);
                xIcon.setPreserveRatio(true);

                Circle circle = new Circle(16, Color.WHITE);
                circle.setStroke(Color.web("#cccccc"));
                circle.setStrokeWidth(2);
                circle.setOpacity(0.4);
                xIcon.setOpacity(0.4);

                StackPane wrapper = new StackPane(circle, xIcon);
                wrapper.setPrefSize(32,32);
                wrapper.setAlignment(Pos.CENTER);

                VBox ct = new VBox(4, wrapper, new Label("CANCELLED"));
                ct.getStyleClass().addAll("status-item", "cancelled");
                ct.setAlignment(Pos.CENTER);

                timeline.getChildren().add(ct);
            }

            return timeline;
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
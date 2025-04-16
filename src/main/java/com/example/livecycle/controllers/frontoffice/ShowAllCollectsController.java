package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Collect;
import com.example.livecycle.services.CollectService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ShowAllCollectsController implements Initializable {

    @FXML private TableView<Collect> collectsTable;
    @FXML private TableColumn<Collect, String> userEmailColumn;
    @FXML private TableColumn<Collect, String> categorieColumn;
    @FXML private TableColumn<Collect, String> titreColumn;
    @FXML private TableColumn<Collect, String> produitColumn;
    @FXML private TableColumn<Collect, Number> quantiteColumn;
    @FXML private TableColumn<Collect, String> lieuColumn;
    @FXML private TableColumn<Collect, String> dateDebutColumn;
    @FXML private FlowPane cardsContainer;


    private final CollectService collectService = new CollectService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCollects();
    }

    private void configureTableColumns() {
        userEmailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserEmail()));
        categorieColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorieCollect().getNom()));
        titreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        produitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomProduit()));
        quantiteColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantite()));
        lieuColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        dateDebutColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateDebut().toString()));
    }

    private void loadCollects() {
        try {
            List<Collect> collects = collectService.recuperer();
            collects.forEach(this::createCollectCard);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private void createCollectCard(Collect collect) {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        Label title = new Label(collect.getTitre());
        title.getStyleClass().add("card-title");

        addCardItem(card, "User Email:", collect.getUserEmail());
        addCardItem(card, "Category:", collect.getCategorieCollect().getNom());
        addCardItem(card, "Product:", collect.getNomProduit());
        addCardItem(card, "Quantity:", String.valueOf(collect.getQuantite()));
        addCardItem(card, "Location:", collect.getLieu());
        addCardItem(card, "Start Date:", collect.getDateDebut().toString());

        cardsContainer.getChildren().add(card);
    }

    private void addCardItem(VBox card, String label, String value) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("card-label");
        lbl.setMinWidth(80);

        Label val = new Label(value);
        val.getStyleClass().add("card-value");
        val.setWrapText(true);

        container.getChildren().addAll(lbl, val);
        card.getChildren().add(container);

        // Add separator after each item except last
        if(card.getChildren().size() < 6) {
            Separator separator = new Separator();
            separator.getStyleClass().add("card-separator");
            card.getChildren().add(separator);
        }
    }

}
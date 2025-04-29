package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.entities.Collect;
import com.example.livecycle.services.CategorieCollectService;
import com.example.livecycle.services.CollectService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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

    @FXML private TextField searchTitre;
    @FXML private TextField searchProduit;
    @FXML private ComboBox<CategorieCollect> searchCategorie;
    @FXML private TextField searchLieu;
    @FXML private DatePicker searchDate;
    @FXML private TextField searchQuantite;



    private final CollectService collectService = new CollectService();
    private final CategorieCollectService categorieService = new CategorieCollectService();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        loadCategories();
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








    private void loadCategories() {
        try {
            List<CategorieCollect> categories = categorieService.recupererToutes();
            searchCategorie.getItems().addAll(categories);
            searchCategorie.setConverter(new StringConverter<>() {
                @Override
                public String toString(CategorieCollect categorie) {
                    return categorie != null ? categorie.getNom() : "";
                }

                @Override
                public CategorieCollect fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load categories");
        }
    }


    @FXML
    private void handleSearch(ActionEvent event) {
        String titre = searchTitre.getText().trim();
        String produit = searchProduit.getText().trim();
        CategorieCollect categorie = searchCategorie.getValue();
        String lieu = searchLieu.getText().trim();
        LocalDate date = searchDate.getValue();
        String quantiteStr = searchQuantite.getText().trim();

        Integer quantite = null;
        try {
            if (!quantiteStr.isEmpty()) {
                quantite = Integer.parseInt(quantiteStr);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Quantity must be a number");
            return;
        }

        try {
            List<Collect> filtered = collectService.rechercheAvancee(
                    titre, produit, categorie, lieu, date, quantite
            );
            updateCards(filtered);
        } catch (SQLException e) {
            showAlert("Search Error", "Failed to perform search: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        searchTitre.clear();
        searchProduit.clear();
        searchCategorie.getSelectionModel().clearSelection();
        searchLieu.clear();
        searchDate.setValue(null);
        searchQuantite.clear();
        loadCollects();
    }

    private void updateCards(List<Collect> collects) {
        cardsContainer.getChildren().clear();
        collects.forEach(this::createCollectCard);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
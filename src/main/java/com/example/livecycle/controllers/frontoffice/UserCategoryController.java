package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserCategoryController implements Initializable {

    @FXML
    private ListView<CategoryForum> listCategories;

    @FXML
    private Button btnAccess;

    private Connection con;
    private PreparedStatement st;
    private ResultSet rs;

    private ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DBConnexion.getCon();
        setupListView();
        loadCategories();

        // Désactive le bouton tant qu'aucune sélection
        btnAccess.disableProperty().bind(
                listCategories.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    private void setupListView() {
        listCategories.setCellFactory(param -> new javafx.scene.control.ListCell<CategoryForum>() {
            private final VBox container = new VBox();
            private final Label titleLabel = new Label();
            private final Label descriptionLabel = new Label();

            {
                container.setSpacing(5);
                container.setPadding(new Insets(15));
                container.getStyleClass().add("category-item");

                titleLabel.getStyleClass().add("category-title");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                descriptionLabel.getStyleClass().add("category-description");
                descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                descriptionLabel.setWrapText(true);

                container.getChildren().addAll(titleLabel, descriptionLabel);
            }

            @Override
            protected void updateItem(CategoryForum category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setGraphic(null);
                } else {
                    titleLabel.setText(category.getName());
                    descriptionLabel.setText(category.getDescription());
                    setGraphic(container);
                }
            }
        });
    }

    public void loadCategories() {
        categories.clear();
        String query = "SELECT * FROM categorie_forum";
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                categories.add(new CategoryForum(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
            listCategories.setItems(categories);
        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Échec du chargement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void goToForum(javafx.event.ActionEvent event) {
        CategoryForum selectedCategory = listCategories.getSelectionModel().getSelectedItem();

        // Double vérification de sécurité
        if (selectedCategory == null) {
            showAlert("Sélection Requise", "Veuillez choisir une catégorie", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/forum.fxml"));
            Parent root = loader.load();

            ForumController forumController = loader.getController();
            forumController.setCategory(selectedCategory);

            Stage stage = new Stage();
            stage.setTitle("Forum - " + selectedCategory.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur Navigation", "Impossible d'ouvrir le forum : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.err.println("Erreur fermeture ressources: " + e.getMessage());
        }
    }
}
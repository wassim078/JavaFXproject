package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class CategoryForumController implements Initializable {

    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;

    @FXML
    private TextField DESCRIPTION, ID, NAME;

    @FXML
    private TableColumn<CategoryForum, Integer> Id;
    @FXML
    private TableColumn<CategoryForum, String> Name, Description;

    @FXML
    private Button btnClear, btnDelete, btnSave, btnUpdate;

    @FXML
    private TableView<CategoryForum> table;

    private ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DBConnexion.getCon();

        // ⚠️ Lier les colonnes aux propriétés de la classe CategoryForum
        Id.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        Name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        Description.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        loadCategories();
    }

    public void loadCategories() {
        categories.clear();
        String query = "SELECT * FROM categorie_forum";
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                categories.add(new CategoryForum(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
            table.setItems(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void createCategory(ActionEvent event) {
        String name = NAME.getText();
        String description = DESCRIPTION.getText();
        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO categorie_forum (name, description) VALUES (?, ?)";
        try {
            st = con.prepareStatement(query);
            st.setString(1, name);
            st.setString(2, description);
            st.executeUpdate();
            showAlert("Succès", "Catégorie ajoutée avec succès", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateCategory(ActionEvent event) {
        if (ID.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez une catégorie à modifier", Alert.AlertType.ERROR);
            return;
        }

        int id = Integer.parseInt(ID.getText());
        String name = NAME.getText();
        String description = DESCRIPTION.getText();

        String query = "UPDATE categorie_forum SET name=?, description=? WHERE id=?";
        try {
            st = con.prepareStatement(query);
            st.setString(1, name);
            st.setString(2, description);
            st.setInt(3, id);
            st.executeUpdate();
            showAlert("Succès", "Catégorie mise à jour", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCategory(ActionEvent event) {
        if (ID.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez une catégorie à supprimer", Alert.AlertType.ERROR);
            return;
        }

        int id = Integer.parseInt(ID.getText());
        String query = "DELETE FROM categorie_forum WHERE id=?";
        try {
            st = con.prepareStatement(query);
            st.setInt(1, id);
            st.executeUpdate();
            showAlert("Succès", "Catégorie supprimée", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearCategory(ActionEvent event) {
        ID.clear();
        NAME.clear();
        DESCRIPTION.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void selectCategory() {
        CategoryForum selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ID.setText(String.valueOf(selected.getId()));
            NAME.setText(selected.getName());
            DESCRIPTION.setText(selected.getDescription());
        }
    }
}

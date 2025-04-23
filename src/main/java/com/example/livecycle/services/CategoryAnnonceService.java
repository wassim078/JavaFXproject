package com.example.livecycle.services;

import com.example.livecycle.entities.Category;
import com.example.livecycle.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryAnnonceService implements Service<Category> {

    @Override
    public List<Category> recuperer() throws SQLException {
        String query = "SELECT * FROM categorie_annonce";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        }
        return categories;
    }

    @Override
    public boolean ajouter(Category category) throws SQLException {
        String query = "INSERT INTO categorie_annonce (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, category.getName());
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Category category) throws SQLException {
        String query = "UPDATE categorie_annonce SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, category.getName());
            pst.setInt(2, category.getId());
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String query = "DELETE FROM categorie_annonce WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        }
    }
}
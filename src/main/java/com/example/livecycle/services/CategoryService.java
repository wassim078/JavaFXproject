package com.example.livecycle.services;

import com.example.livecycle.entities.Category;
import com.example.livecycle.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService implements Service<Category> {

    @Override
    public List<Category> recuperer() throws SQLException {
        String query = "SELECT * FROM categorie_annonce";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                categories.add(category);
            }
        }
        return categories;
    }

    // Implement other CRUD methods as needed
    @Override public boolean ajouter(Category t) throws SQLException { return false; }
    @Override public boolean modifier(Category t) throws SQLException { return false; }
    @Override public boolean supprimer(int id) throws SQLException { return false; }
}
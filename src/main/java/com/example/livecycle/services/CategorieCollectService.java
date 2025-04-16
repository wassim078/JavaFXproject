package com.example.livecycle.services;

import com.example.livecycle.entities.CategorieCollect;
import com.example.livecycle.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieCollectService implements Service<CategorieCollect> {

    @Override
    public boolean ajouter(CategorieCollect categorie) throws SQLException {
        String query = "INSERT INTO categorie_collect (nom, description) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query)) {
            ps.setString(1, categorie.getNom());
            ps.setString(2, categorie.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(CategorieCollect categorieCollect) throws SQLException {
        return false;
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        return false;
    }

    @Override
    public List<CategorieCollect> recuperer() throws SQLException {
        List<CategorieCollect> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie_collect";
        try (Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                CategorieCollect categorie = new CategorieCollect();
                categorie.setId(rs.getInt("id"));
                categorie.setNom(rs.getString("nom"));
                categorie.setDescription(rs.getString("description"));
                categories.add(categorie);
            }
        }
        return categories;
    }

    // Implement modifier and supprimer methods
}
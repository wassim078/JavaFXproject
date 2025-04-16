package com.example.livecycle.services;

import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.CategorieCollect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.livecycle.utils.DatabaseConnection;

public class CollectService implements Service<Collect> {








    @Override
    public boolean ajouter(Collect collect) throws SQLException {
        String query = "INSERT INTO collect (categorie_collect_id, titre, nom_produit, quantite, lieu, date_debut, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query)) {
            ps.setInt(1, collect.getCategorieCollect().getId());
            ps.setString(2, collect.getTitre());
            ps.setString(3, collect.getNomProduit());
            ps.setInt(4, collect.getQuantite());
            ps.setString(5, collect.getLieu());
            ps.setDate(6, Date.valueOf(collect.getDateDebut()));
            ps.setInt(7, collect.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Collect collect) throws SQLException {
        // Update the record in the collect table based on the collect id.
        String query = "UPDATE collect SET categorie_collect_id = ?, titre = ?, nom_produit = ?, quantite = ?, lieu = ?, date_debut = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query)) {
            ps.setInt(1, collect.getCategorieCollect().getId());
            ps.setString(2, collect.getTitre());
            ps.setString(3, collect.getNomProduit());
            ps.setInt(4, collect.getQuantite());
            ps.setString(5, collect.getLieu());
            ps.setDate(6, Date.valueOf(collect.getDateDebut()));
            ps.setInt(7, collect.getUserId());
            ps.setInt(8, collect.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        // Remove the collect entry from the table using the provided id.
        String query = "DELETE FROM collect WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Collect> recuperer() throws SQLException {
        List<Collect> collects = new ArrayList<>();
        String query = "SELECT c.*, cc.nom AS categorie_nom, u.email AS user_email " +
                "FROM collect c " +
                "JOIN categorie_collect cc ON c.categorie_collect_id = cc.id " +
                "JOIN user u ON c.user_id = u.id";

        try (Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Collect collect = mapResultSetToCollect(rs);
                collect.setUserEmail(rs.getString("user_email")); // Add this line
                collects.add(collect);
            }
        }
        return collects;
    }

    public List<Collect> recupererParUtilisateur(int userId) throws SQLException {
        List<Collect> collects = new ArrayList<>();
        String query = "SELECT c.*, cc.nom AS categorie_nom FROM collect c JOIN categorie_collect cc ON c.categorie_collect_id = cc.id WHERE c.user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Collect collect = mapResultSetToCollect(rs);
                    collects.add(collect);
                }
            }
        }
        return collects;
    }

    private Collect mapResultSetToCollect(ResultSet rs) throws SQLException {
        Collect collect = new Collect();
        collect.setId(rs.getInt("id"));
        CategorieCollect categorie = new CategorieCollect();
        categorie.setId(rs.getInt("categorie_collect_id"));
        categorie.setNom(rs.getString("categorie_nom"));
        collect.setCategorieCollect(categorie);
        collect.setTitre(rs.getString("titre"));
        collect.setNomProduit(rs.getString("nom_produit"));
        collect.setQuantite(rs.getInt("quantite"));
        collect.setLieu(rs.getString("lieu"));
        collect.setDateDebut(rs.getDate("date_debut").toLocalDate());
        collect.setUserId(rs.getInt("user_id"));
        return collect;
    }


}
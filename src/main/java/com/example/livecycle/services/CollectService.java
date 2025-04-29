package com.example.livecycle.services;

import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.CategorieCollect;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    public List<Collect> rechercheAvancee(String titre, String produit, CategorieCollect categorie,
                                          String lieu, LocalDate date, Integer quantite) throws SQLException {
        List<Collect> results = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT c.*, cc.nom AS categorie_nom, u.email AS user_email " +
                        "FROM collect c " +
                        "JOIN categorie_collect cc ON c.categorie_collect_id = cc.id " +
                        "JOIN user u ON c.user_id = u.id " +
                        "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (!titre.isEmpty()) {
            query.append(" AND c.titre LIKE ?");
            params.add("%" + titre + "%");
        }
        if (!produit.isEmpty()) {
            query.append(" AND c.nom_produit LIKE ?");
            params.add("%" + produit + "%");
        }
        if (categorie != null) {
            query.append(" AND c.categorie_collect_id = ?");
            params.add(categorie.getId());
        }
        if (!lieu.isEmpty()) {
            query.append(" AND c.lieu LIKE ?");
            params.add("%" + lieu + "%");
        }
        if (date != null) {
            query.append(" AND c.date_debut = ?");  // Changed to exact date
            params.add(Date.valueOf(date));
        }
        if (quantite != null) {
            query.append(" AND c.quantite = ?");  // Changed to exact quantity
            params.add(quantite);
        }

        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Collect collect = mapResultSetToCollect(rs);
                    collect.setUserEmail(rs.getString("user_email"));
                    results.add(collect);
                }
            }
        }
        return results;
    }


    public Map<String, Integer> getCollectTrend(String period) {
        Map<String, Integer> trend = new LinkedHashMap<>();
        String dateFormat;
        String sqlDateTrunc;

        switch (period.toLowerCase()) {
            case "daily":
                dateFormat = "yyyy-MM-dd";
                sqlDateTrunc = "DATE_FORMAT(date_debut, '%Y-%m-%d')";
                break;
            case "weekly":
                dateFormat = "yyyy-'W'ww";
                sqlDateTrunc = "DATE_FORMAT(date_debut, '%x-%v')";  // ISO week format
                break;
            default: // monthly
                dateFormat = "yyyy-MM";
                sqlDateTrunc = "DATE_FORMAT(date_debut, '%Y-%m')";
                break;
        }

        String query = "SELECT " + sqlDateTrunc + " AS period, COUNT(*) AS count " +
                "FROM collect GROUP BY period ORDER BY MIN(date_debut)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String key = rs.getString("period");
                // Format weekly results consistently
                if ("weekly".equals(period.toLowerCase())) {
                    key = key.replace("-", "-W");
                }
                trend.put(key, rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trend;
    }

    public Map<String, Integer> getCollectsPerUser(String sortOrder, int limit) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        String orderBy;
        switch (sortOrder.toLowerCase()) {
            case "least collects": orderBy = "ASC"; break;
            case "alphabetical": orderBy = "u.email ASC"; break;
            default: orderBy = "DESC"; break; // Most Collects
        }

        String query = "SELECT u.email, COUNT(c.id) as collect_count " +
                "FROM user u " +
                "LEFT JOIN collect c ON u.id = c.user_id " +
                "GROUP BY u.id " +
                "ORDER BY " + (sortOrder.equals("Alphabetical") ? "u.email ASC" : "collect_count " + orderBy) + " " +
                "LIMIT ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, limit > 0 ? limit : 10);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    int count = rs.getInt("collect_count");
                    stats.put(email, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }


}
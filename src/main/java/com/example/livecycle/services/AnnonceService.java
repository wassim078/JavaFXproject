package com.example.livecycle.services;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.utils.DatabaseConnection;
import com.example.livecycle.utils.sms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

public class AnnonceService implements Service<Annonce> {
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Override
    public boolean ajouter(Annonce annonce) throws SQLException {
        String query = "INSERT INTO annonce (user_id, titre, poids, prix, description, image, quantite, categorie_annonce_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            String imagePath = saveImage(annonce.getImage());

            pst.setInt(1, annonce.getUserId());
            pst.setString(2, annonce.getTitre());
            pst.setDouble(3, annonce.getPoids());
            pst.setDouble(4, annonce.getPrix());
            pst.setString(5, annonce.getDescription());
            pst.setString(6, imagePath);
            pst.setInt(7, annonce.getQuantite());
            pst.setInt(8, annonce.getCategorieAnnonce().getId());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Annonce annonce) throws SQLException {
        // Handle image update
        String currentImage = getCurrentImagePath(annonce.getId());
        String newImagePath = annonce.getImage();

        if (!newImagePath.equals(currentImage)){
            if (!newImagePath.startsWith(UPLOAD_DIR)) {
                newImagePath = saveImage(newImagePath);
                annonce.setImage(newImagePath);
            }
        }

        String query = "UPDATE annonce SET " +
                "titre = ?, categorie_annonce_id = ?, poids = ?, prix = ?, " +
                "description = ?, image = ?, quantite = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, annonce.getTitre());
            pst.setInt(2, annonce.getCategorieAnnonce().getId());
            pst.setDouble(3, annonce.getPoids());
            pst.setDouble(4, annonce.getPrix());
            pst.setString(5, annonce.getDescription());
            pst.setString(6, annonce.getImage());
            pst.setInt(7, annonce.getQuantite());
            pst.setInt(8, annonce.getId());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String query = "DELETE FROM annonce WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, id);
            boolean isDeleted = pst.executeUpdate() > 0;

            if (isDeleted) {
                // Envoi de SMS après suppression réussie
                sms smsSender = new sms();
                String adminPhoneNumber = "55017213"; // Numéro à remplacer par le tien ou celui d’un admin
                String message = "L'annonce avec l'ID " + id + " a été supprimée du système.";
                smsSender.envoyerSms(adminPhoneNumber, message);
            }

            return isDeleted;
        }
    }


    @Override
    public List<Annonce> recuperer() throws SQLException {
        String query = "SELECT a.*, c.name AS category_name FROM annonce a " +
                "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id";
        return getByQuery(query);
    }

    public List<Annonce> getByUserId(int userId) throws SQLException {
        String query = "SELECT a.*, c.name AS category_name FROM annonce a " +
                "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id " +
                "WHERE a.user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            return getByQuery(pst);
        }
    }

    private List<Annonce> getByQuery(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            return getByQuery(pst);
        }
    }

    private List<Annonce> getByQuery(PreparedStatement pst) throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Annonce annonce = mapResultSetToAnnonce(rs);
                annonces.add(annonce);
            }
        }
        return annonces;
    }

    private Annonce mapResultSetToAnnonce(ResultSet rs) throws SQLException {
        Annonce annonce = new Annonce();
        annonce.setId(rs.getInt("id"));
        annonce.setCategorieAnnonceId(rs.getInt("categorie_annonce_id"));
        annonce.setUserId(rs.getInt("user_id"));
        annonce.setTitre(rs.getString("titre"));
        annonce.setPoids(rs.getDouble("poids"));
        annonce.setPrix(rs.getDouble("prix"));
        annonce.setDescription(rs.getString("description"));
        annonce.setImage(rs.getString("image"));
        annonce.setQuantite(rs.getInt("quantite"));

        Category category = new Category();
        category.setId(rs.getInt("categorie_annonce_id"));
        category.setName(rs.getString("category_name"));
        annonce.setCategorieAnnonce(category);

        try {
            annonce.setFavori(rs.getBoolean("is_favori"));
        } catch (SQLException e) {
            // Si la colonne n'existe pas dans le ResultSet, on ignore
            annonce.setFavori(false);
        }

        return annonce;
    }

    private String saveImage(String imagePath) {
        try {
            File source = new File(imagePath);
            File destDir = new File(UPLOAD_DIR);
            if (!destDir.exists()) {
                destDir.mkdirs(); // Ensure directory exists
            }
            String destPath = destDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + "_" + source.getName();
            Files.copy(source.toPath(), new File(destPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destPath; // Return absolute path
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return "";
        }
    }


    private String getCurrentImagePath(int annonceId) throws SQLException {
        String query = "SELECT image FROM annonce WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, annonceId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image");
                }
            }
        }
        return "";
    }


    public List<Annonce> getAllWithUserEmail() throws SQLException {
        String query = "SELECT a.*, c.name AS category_name, u.email AS user_email " +
                "FROM annonce a " +
                "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id " +
                "JOIN user u ON a.user_id = u.id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            return getByQueryWithEmail(pst);
        }
    }
    private List<Annonce> getByQueryWithEmail(PreparedStatement pst) throws SQLException {
        List<Annonce> annonces = new ArrayList<>();
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Annonce annonce = mapResultSetToAnnonce(rs);
                annonce.setUserEmail(rs.getString("user_email")); // Temporary field
                annonces.add(annonce);
            }
        }
        return annonces;
    }

    public Annonce getById(int id) throws SQLException {
        String query = "SELECT a.*, c.name AS category_name FROM annonce a " +
                "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id " +
                "WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnnonce(rs);
                }
            }
        }
        return null;
    }

    public List<Annonce> recupererPaginated(int offset, int limit) throws SQLException {
        String query = "SELECT a.*, c.name AS category_name FROM annonce a " +
                "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id " +
                "ORDER BY a.id DESC " +
                "LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, limit);
            pst.setInt(2, offset);
            return getByQuery(pst);
        }
    }

    public int getTotalAnnoncesCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM annonce";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean toggleFavori(int userId, int annonceId) throws SQLException {
        String checkQuery = "SELECT * FROM annonce_favoris WHERE user_id = ? AND annonce_id = ?";
        String insertQuery = "INSERT INTO annonce_favoris (user_id, annonce_id) VALUES (?, ?)";
        String deleteQuery = "DELETE FROM annonce_favoris WHERE user_id = ? AND annonce_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkPst = conn.prepareStatement(checkQuery)) {
            
            checkPst.setInt(1, userId);
            checkPst.setInt(2, annonceId);
            
            try (ResultSet rs = checkPst.executeQuery()) {
                if (rs.next()) {
                    // Si existe, supprimer des favoris
                    try (PreparedStatement deletePst = conn.prepareStatement(deleteQuery)) {
                        deletePst.setInt(1, userId);
                        deletePst.setInt(2, annonceId);
                        return deletePst.executeUpdate() > 0;
                    }
                } else {
                    // Si n'existe pas, ajouter aux favoris
                    try (PreparedStatement insertPst = conn.prepareStatement(insertQuery)) {
                        insertPst.setInt(1, userId);
                        insertPst.setInt(2, annonceId);
                        return insertPst.executeUpdate() > 0;
                    }
                }
            }
        }
    }

    public List<Annonce> recupererAvecFavoris(int userId) throws SQLException {
        String query = "SELECT a.*, c.name AS category_name, " +
                      "CASE WHEN af.user_id IS NOT NULL THEN TRUE ELSE FALSE END as is_favori " +
                      "FROM annonce a " +
                      "JOIN categorie_annonce c ON a.categorie_annonce_id = c.id " +
                      "LEFT JOIN annonce_favoris af ON a.id = af.annonce_id AND af.user_id = ? " +
                      "ORDER BY is_favori DESC, a.id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setInt(1, userId);
            List<Annonce> annonces = new ArrayList<>();
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Annonce annonce = mapResultSetToAnnonce(rs);
                    annonce.setFavori(rs.getBoolean("is_favori"));
                    annonces.add(annonce);
                }
            }
            return annonces;
        }
    }

    public Map<String, Integer> getAnnoncesTrend(String period) throws SQLException {
        String query = switch (period.toLowerCase()) {
            case "daily" -> "SELECT DATE(NOW() - INTERVAL (a.id % 7) DAY) as period, COUNT(*) as count " +
                           "FROM annonce a " +
                           "GROUP BY DATE(NOW() - INTERVAL (a.id % 7) DAY) " +
                           "ORDER BY period";
            case "weekly" -> "SELECT DATE_FORMAT(NOW() - INTERVAL (a.id % 8) WEEK, '%Y-%u') as period, COUNT(*) as count " +
                           "FROM annonce a " +
                           "GROUP BY DATE_FORMAT(NOW() - INTERVAL (a.id % 8) WEEK, '%Y-%u') " +
                           "ORDER BY period";
            default -> "SELECT DATE_FORMAT(NOW() - INTERVAL (a.id % 6) MONTH, '%Y-%m') as period, COUNT(*) as count " +
                      "FROM annonce a " +
                      "GROUP BY DATE_FORMAT(NOW() - INTERVAL (a.id % 6) MONTH, '%Y-%m') " +
                      "ORDER BY period";
        };

        Map<String, Integer> trend = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                trend.put(rs.getString("period"), rs.getInt("count"));
            }
        }
        return trend;
    }

    public Map<String, Integer> getCategoryDistribution() throws SQLException {
        String query = "SELECT c.name, COUNT(a.id) as count " +
                      "FROM categorie_annonce c " +
                      "LEFT JOIN annonce a ON c.id = a.categorie_annonce_id " +
                      "GROUP BY c.id, c.name";

        Map<String, Integer> distribution = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                distribution.put(rs.getString("name"), rs.getInt("count"));
            }
        }
        return distribution;
    }

    public Map<String, Object> getGeneralStats() throws SQLException {
        String query = "SELECT " +
                      "COUNT(*) as total, " +
                      "COUNT(CASE WHEN quantite > 0 THEN 1 END) as active, " +
                      "AVG(prix) as avg_price " +
                      "FROM annonce";

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("active", rs.getInt("active"));
                stats.put("avgPrice", rs.getDouble("avg_price"));
            }
        }
        return stats;
    }
}
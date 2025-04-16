package com.example.livecycle.services;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Category;
import com.example.livecycle.utils.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            return pst.executeUpdate() > 0;
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
}
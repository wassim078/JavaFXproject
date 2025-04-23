package com.example.livecycle.services;


import com.example.livecycle.entities.Reclamation;
import com.example.livecycle.entities.User;
import com.example.livecycle.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationDAO {

   private Connection connection;

    public ReclamationDAO() {
        try {
            connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize ReclamationDAO", e);
        }
    }

    // Create
    public boolean addReclamation(Reclamation reclamation) throws IllegalArgumentException {
        if (reclamation == null) {
            throw new IllegalArgumentException("Reclamation cannot be null");
        }
        if (reclamation.getUser() == null) {
            throw new IllegalArgumentException("Reclamation must have an associated user");
        }

        String query = "INSERT INTO reclamation (sujet, description, type, etat, date, file, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, reclamation.getSujet());
            statement.setString(2, reclamation.getDescription());
            statement.setString(3, reclamation.getType());
            statement.setString(4, reclamation.getEtat());
            statement.setTimestamp(5, Timestamp.valueOf(reclamation.getDate()));
            statement.setString(6, reclamation.getFile());
            statement.setInt(7, reclamation.getUser().getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reclamation.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add reclamation", e);
        }
    }

    // Read (single)
    public Reclamation getReclamationById(int id) {
        String query = "SELECT r.id AS reclamation_id, r.sujet, r.description, r.type, r.etat, r.date, r.file, r.user_id, "
                + "u.id AS user_id, u.prenom, u.nom, u.email, u.password, u.adresse, u.telephone, u.roles, u.image "
                + "FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToReclamation(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get reclamation by id: " + id, e);
        }
    }

    // Read (all)
    public List<Reclamation> getAllReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.id AS reclamation_id, r.sujet, r.description, r.type, r.etat, r.date, r.file, r.user_id, "
                + "u.id AS user_id, u.prenom, u.nom, u.email, u.password, u.adresse, u.telephone, u.roles, u.image "
                + "FROM reclamation r JOIN user u ON r.user_id = u.id";


        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                reclamations.add(mapResultSetToReclamation(resultSet));
            }
            return reclamations;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all reclamations", e);
        }
    }

    // Read (by user)
    // In ReclamationDAO.java
    public List<Reclamation> getReclamationsByUser(int userId) {
        System.out.println("DEBUG: Fetching complaints for user ID: " + userId);
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.id AS reclamation_id, r.sujet, r.description, r.type, r.etat, r.date, r.file, r.user_id, "
                + "u.id AS user_id, u.prenom, u.nom, u.email, u.password, u.adresse, u.telephone, u.roles, u.image "
                + "FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.user_id = ?"; // ✅ Clean SQL

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                System.out.println("DEBUG: Found complaint - ID: " + resultSet.getInt("reclamation_id")); // ✅ Fixed alias
                reclamations.add(mapResultSetToReclamation(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG: Total complaints found: " + reclamations.size());
        return reclamations;
    }

    // Update
    public boolean updateReclamation(Reclamation reclamation) {
        if (reclamation == null || reclamation.getUser() == null) {
            return false;
        }

        String query = "UPDATE reclamation SET sujet = ?, description = ?, type = ?, etat = ?, date = ?, file = ?, user_id = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reclamation.getSujet());
            statement.setString(2, reclamation.getDescription());
            statement.setString(3, reclamation.getType());
            statement.setString(4, reclamation.getEtat());
            statement.setTimestamp(5, Timestamp.valueOf(reclamation.getDate()));
            statement.setString(6, reclamation.getFile());
            statement.setInt(7, reclamation.getUser().getId());
            statement.setInt(8, reclamation.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reclamation: " + reclamation.getId(), e);
        }
    }

    // Delete
    public boolean deleteReclamation(int id) {
        String query = "DELETE FROM reclamation WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reclamation: " + id, e);
        }
    }

    // Helper method to map ResultSet to Reclamation object
    private Reclamation mapResultSetToReclamation(ResultSet resultSet) throws SQLException {
        Reclamation reclamation = new Reclamation();
        reclamation.setId(resultSet.getInt("reclamation_id"));
        reclamation.setSujet(resultSet.getString("sujet"));
        reclamation.setDescription(resultSet.getString("description"));
        reclamation.setType(resultSet.getString("type"));
        reclamation.setEtat(resultSet.getString("etat"));
        reclamation.setDate(resultSet.getTimestamp("date").toLocalDateTime());
        reclamation.setFile(resultSet.getString("file"));

        // Create and set User object
        User user = new User(
                resultSet.getString("prenom"),
                resultSet.getString("nom"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("adresse"),
                resultSet.getString("telephone"),
                resultSet.getString("roles"),
                resultSet.getString("image")
        );
        user.setId(resultSet.getInt("user_id"));
        reclamation.setUser(user);

        return reclamation;
    }
}
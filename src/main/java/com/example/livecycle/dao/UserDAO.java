package com.example.livecycle.dao;

import com.example.livecycle.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO {


    private static final String INSERT_USER_SQL = "INSERT INTO user " +
            "(prenom, nom, email, password, adresse, telephone,roles, image) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_USER_BY_ID = "SELECT * FROM user WHERE id = ?";

    private static final String UPDATE_USER_SQL = "UPDATE user SET prenom = ?, nom = ?, email = ?, password = ?, adresse = ?, telephone = ?, roles = ?, image = ? WHERE id = ?";

    private static final String DELETE_USER = "DELETE FROM user WHERE id = ?";

    String sql = "SELECT * FROM user WHERE email = ?";

    private static final String SELECT_ALL_USERS = "SELECT * FROM user";






    public boolean createUser(User user) {
        try {
            // Obtenir la connexion via le Singleton
            Connection conn = DatabaseConnection.getInstance().getConnection();

            PreparedStatement pstmt = conn.prepareStatement(INSERT_USER_SQL);

            String rolesJson = "[\"" + user.getRoles() + "\"]";

            String hashedPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());


            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashedPw);
            pstmt.setString(5, user.getAdresse());
            pstmt.setString(6, user.getTelephone());
            pstmt.setString(7, rolesJson);
            pstmt.setString(8, user.getImage());
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User authenticateUser(String email, String password) {


        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Verify password with BCrypt
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new User(
                            rs.getString("prenom"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            storedHash,  // Store hash, not plain text
                            rs.getString("adresse"),
                            rs.getString("telephone"),
                            rs.getString("roles"),  // JSON string from DB
                            rs.getString("image")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        System.out.println("[DEBUG] Executing SQL: " + SELECT_ALL_USERS);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_USERS);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("SQL Query: " + SELECT_ALL_USERS);
            System.out.println("Connection valid? " + conn.isValid(2));

            while (rs.next()) {
                System.out.println("Found user record:");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Prenom: " + rs.getString("prenom"));

                User user = new User(
                        rs.getString("prenom"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("roles"),  // Verify this column exists
                        rs.getString("image")   // Verify this column exists
                );
                user.setId(rs.getInt("id"));
                users.add(user);
            }
            System.out.println("Total users loaded: " + users.size());

        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_USER)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }

    }



    public User getUser(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_USER_BY_ID)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("prenom"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("adresse"),
                            rs.getString("telephone"),
                            rs.getString("roles"),
                            rs.getString("image")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }



    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }
    public boolean addUser(User user) {
        // Update your SQL insert statement to include password
        String sql = "INSERT INTO user (prenom, nom, email, password, adresse, telephone,roles, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String hashedPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashedPw);  // Store hashed password
            pstmt.setString(5, user.getAdresse());
            pstmt.setString(6, user.getTelephone());
            pstmt.setString(7, user.getRoles());
            pstmt.setString(8, user.getImage());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_USER_SQL)) {

            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword()); // Make sure the password is already hashed if needed
            pstmt.setString(5, user.getAdresse());
            pstmt.setString(6, user.getTelephone());
            pstmt.setString(7, user.getRoles());
            pstmt.setString(8, user.getImage());
            pstmt.setInt(9, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




}
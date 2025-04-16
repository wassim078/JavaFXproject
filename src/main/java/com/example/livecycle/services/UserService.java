package com.example.livecycle.services;

import com.example.livecycle.entities.User;
import com.example.livecycle.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UserService implements Service<User> {
    private static final String UPDATE_USER_SQL = "UPDATE user SET " +
            "prenom = ?, nom = ?, email = ?, password = ?, adresse = ?, telephone = ?, image = ? " +
            "WHERE id = ?";


    private static final String INSERT_USER_SQL = "INSERT INTO user " +
            "(prenom, nom, email, password, adresse, telephone,roles, image) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_USER_BY_ID = "SELECT * FROM user WHERE id = ?";

    private static final String UPDATE_USER_SQL2 = "UPDATE user SET prenom = ?, nom = ?, email = ?, password = ?, adresse = ?, telephone = ?, roles = ?, image = ? WHERE id = ?";

    private static final String DELETE_USER = "DELETE FROM user WHERE id = ?";

    String sql = "SELECT * FROM user WHERE email = ?";

    private static final String SELECT_ALL_USERS = "SELECT * FROM user";

    @Override
    public boolean ajouter(User user) throws SQLException {
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

    @Override
    public List<User> recuperer() throws SQLException {
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


    @Override
    public boolean supprimer(int id) throws SQLException {
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


    @Override
    public boolean modifier(User user) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_USER_SQL2)) {

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


    public void enableUser(String email) {
        String sql = "UPDATE user SET enabled = true WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error enabling user: " + e.getMessage());
        }
    }


    public boolean createGoogleUser(User user) {
        String sql = "INSERT INTO user (prenom, nom, email, password, adresse, telephone, roles, image, enabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String rolesJson = user.getRoles();
            String hashedPw = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, hashedPw);
            pstmt.setString(5, user.getAdresse());
            pstmt.setString(6, user.getTelephone());
            pstmt.setString(7, rolesJson);
            pstmt.setString(8, user.getImage());
            pstmt.setBoolean(9, true); // enabled = true

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating Google user: " + e.getMessage());
            return false;
        }
    }


    public boolean updateUserProfile(User user) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_USER_SQL)) {

            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword()); // This is crucial
            pstmt.setString(5, user.getAdresse());
            pstmt.setString(6, user.getTelephone());
            pstmt.setString(7, user.getImage());
            pstmt.setInt(8, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
            return false;
        }
    }

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
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    User user = mapUserFromResultSet(rs);
                    user.setEnabled(rs.getBoolean("enabled"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public User getUserByVerificationToken(String token) {
        String sql = "SELECT * FROM user WHERE verification_token = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by verification token: " + e.getMessage());
        }
        return null;
    }


    public User getUser(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_USER_BY_ID)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs); // Use the mapper that includes enabled status
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


    //GOOGLE AUTHENTICATION

    public User authenticateGoogleUser(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("prenom"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("roles"),
                        rs.getString("image")
                );
                // ADD THIS LINE
                user.setId(rs.getInt("id"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean createUserWithVerification(User user) {
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEnabled(false);

        String sql = "INSERT INTO user (prenom, nom, email, password, adresse, telephone, roles, image, enabled, verification_token) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
            pstmt.setBoolean(9, user.isEnabled());
            pstmt.setString(10, user.getVerificationToken());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("User creation error: " + e.getMessage());
            return false;
        }
    }

    // Verification method

    public boolean verifyUser(String token) {
        String sql = "UPDATE user SET enabled = true WHERE verification_token = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Verification error: " + e.getMessage());
            return false;
        }
    }

    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("prenom"),
                rs.getString("nom"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                rs.getString("roles"),
                rs.getString("image")
        );
        user.setId(rs.getInt("id"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setVerificationToken(rs.getString("verification_token"));
        return user;
    }

    public void createPasswordResetToken(String email, String token) {
        String sql = "UPDATE user SET reset_token = ?, reset_expires = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setString(2, email);
            int updated = pstmt.executeUpdate();
            System.out.println("Updated rows for reset token: " + updated); // Debug logging
        } catch (SQLException e) {
            System.err.println("Error setting reset token: " + e.getMessage());
            e.printStackTrace(); // Add stack trace
        }
    }
    public boolean resetPassword(String token, String newPassword) {
        String sql = "UPDATE user SET password = ?, reset_token = NULL, reset_expires = NULL " +
                "WHERE reset_token = ? AND reset_expires > NOW()";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String hashedPw = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            pstmt.setString(1, hashedPw);
            pstmt.setString(2, token);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Password reset error: " + e.getMessage());
            return false;
        }
    }


    public boolean isValidResetToken(String token) {
        String sql = "SELECT reset_token, reset_expires FROM user WHERE reset_token = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp expires = rs.getTimestamp("reset_expires");
                    return expires != null && expires.after(new Timestamp(System.currentTimeMillis()));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating reset token: " + e.getMessage());
        }
        return false;
    }





}
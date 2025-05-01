package com.example.livecycle.services;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.utils.DatabaseConnection;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CommandeService implements Service<Commande> {
    // Constants
    private static final List<String> ALLOWED_PAIEMENT = Arrays.asList(
            "E-paiement",
            "à la livraison"
    );

    public static final List<String> ORDER_STATES = Arrays.asList(
            "PENDING",
            "AWAITING_DELIVERY",
            "PAID",
            "PROCESSING",
            "SHIPPED",
            "DELIVERED",
            "CANCELLED"
    );

    private static final String DEFAULT_CLIENT_NAME = "";
    private static final String DEFAULT_CLIENT_FAMILY_NAME = "";
    private static final String DEFAULT_CLIENT_ADDRESS = "";
    private static final String DEFAULT_CLIENT_PHONE = "";
    private static final String DEFAULT_PAIEMENT_METHOD = "E-paiement";
    private static final String DEFAULT_ORDER_STATE = "PENDING";
    private static final String DEFAULT_INSTRUCTION_SPECIAL = "";
    private static final double DEFAULT_PRIX_TOTAL = 0.0;

    // Validation methods
    private boolean isValidMethodePaiement(String methode) {
        return ALLOWED_PAIEMENT.contains(methode);
    }

    public boolean isValidEtatCommande(String etat) {
        return ORDER_STATES.contains(etat);
    }

    // Core CRUD operations
    @Override
    public boolean ajouter(Commande commande) throws SQLException {
        if (!isValidMethodePaiement(commande.getMethodePaiement())) {
            throw new IllegalArgumentException("Méthode de paiement non valide");
        }
        if (!isValidEtatCommande(commande.getEtatCommande())) {
            throw new IllegalArgumentException("État de commande non valide");
        }

        String sql = "INSERT INTO commande (user_id, client_name, client_family_name, "
                + "client_adresse, client_phone, annonce_quantities, methode_paiement, "
                + "etat_commande, date, instruction_speciale, prixtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, commande.getUserId());
            pst.setString(2, commande.getClientName());
            pst.setString(3, commande.getClientFamilyName());
            pst.setString(4, commande.getClientAddress());
            pst.setString(5, commande.getClientPhone());
            pst.setString(6, commande.getAnnonceQuantities());
            pst.setString(7, commande.getMethodePaiement());
            pst.setString(8, commande.getEtatCommande());
            pst.setTimestamp(9, Timestamp.valueOf(commande.getDate()));
            pst.setString(10, commande.getInstructionSpeciale());
            pst.setDouble(11, commande.getPrixTotal());

            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Commande commande) throws SQLException {
        if (!isValidMethodePaiement(commande.getMethodePaiement())) {
            throw new IllegalArgumentException("Méthode de paiement non valide");
        }
        if (!isValidEtatCommande(commande.getEtatCommande())) {
            throw new IllegalArgumentException("État de commande non valide");
        }

        String sql = "UPDATE commande SET user_id = ?, client_name = ?, client_family_name = ?, "
                + "client_adresse = ?, client_phone = ?, annonce_quantities = ?, "
                + "methode_paiement = ?, etat_commande = ?, date = ?, "
                + "instruction_speciale = ?, prixtotal = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, commande.getUserId());
            pst.setString(2, commande.getClientName());
            pst.setString(3, commande.getClientFamilyName());
            pst.setString(4, commande.getClientAddress());
            pst.setString(5, commande.getClientPhone());
            pst.setString(6, commande.getAnnonceQuantities());
            pst.setString(7, commande.getMethodePaiement());
            pst.setString(8, commande.getEtatCommande());
            pst.setTimestamp(9, Timestamp.valueOf(commande.getDate()));
            pst.setString(10, commande.getInstructionSpeciale());
            pst.setDouble(11, commande.getPrixTotal());
            pst.setInt(12, commande.getId());

            return pst.executeUpdate() > 0;
        }
    }

    // State management
    public boolean updateOrderState(int commandeId, String newState) throws SQLException {
        if (!isValidEtatCommande(newState)) {
            throw new IllegalArgumentException("État de commande non valide: " + newState);
        }

        String sql = "UPDATE commande SET etat_commande = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newState);
            pst.setInt(2, commandeId);
            return pst.executeUpdate() > 0;
        }
    }

    // Additional operations
    @Override
    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM commande WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public List<Commande> recuperer() throws SQLException {
        String sql = "SELECT * FROM commande";
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
            }
        }
        return commandes;
    }

    public List<Commande> getByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM commande WHERE user_id = ?";
        List<Commande> commandes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapResultSetToCommande(rs));
                }
            }
        }
        return commandes;
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));
        commande.setUserId(rs.getInt("user_id"));
        commande.setClientName(rs.getString("client_name"));
        commande.setClientFamilyName(rs.getString("client_family_name"));
        commande.setClientAddress(rs.getString("client_adresse"));
        commande.setClientPhone(rs.getString("client_phone"));
        commande.setAnnonceQuantities(rs.getString("annonce_quantities"));
        commande.setMethodePaiement(rs.getString("methode_paiement"));
        commande.setEtatCommande(rs.getString("etat_commande"));
        commande.setDate(rs.getTimestamp("date").toLocalDateTime());
        commande.setInstructionSpeciale(rs.getString("instruction_speciale"));
        commande.setPrixTotal(rs.getDouble("prixtotal"));
        return commande;
    }

    // Utility methods
    public static List<String> getAllowedPaymentMethods() {
        return ALLOWED_PAIEMENT;
    }

    public static List<String> getOrderStates() {
        return ORDER_STATES;
    }


    public Map<String, Integer> getCommandesTrend(String period) {
        Map<String, Integer> trendData = new LinkedHashMap<>();
        String sql = "";

        switch (period.toLowerCase()) {
            case "daily":
                sql = "SELECT DATE(date) as day, COUNT(*) FROM commande GROUP BY day ORDER BY day";
                break;
            case "weekly":
                sql = "SELECT YEARWEEK(date) as week, COUNT(*) FROM commande GROUP BY week ORDER BY week";
                break;
            case "monthly":
                sql = "SELECT DATE_FORMAT(date, '%Y-%m') as month, COUNT(*) FROM commande GROUP BY month ORDER BY month";
                break;
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                trendData.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trendData;
    }


    public Map<String, Integer> getPaymentMethodDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT methode_paiement, COUNT(*) FROM commande GROUP BY methode_paiement";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                distribution.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return distribution;
    }

}
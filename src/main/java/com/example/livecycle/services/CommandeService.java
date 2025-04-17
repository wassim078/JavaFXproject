package com.example.livecycle.services;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.utils.DatabaseConnection;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandeService implements Service<Commande> {
    // Allowed payment methods
    private static final List<String> ALLOWED_PAIEMENT = Arrays.asList(
            "E-paiement",
            "à la livraison"
    );

    // Default values for cart insertion

    private static final String DEFAULT_CLIENT_NAME          = "";
    private static final String DEFAULT_CLIENT_FAMILY_NAME   = "";
    private static final String DEFAULT_CLIENT_ADDRESS       = "";
    private static final String DEFAULT_CLIENT_PHONE         = "";
    private static final String DEFAULT_PAIEMENT_METHOD      = "E-paiement";
    private static final String DEFAULT_ORDER_STATE         = "PENDING";
    private static final String DEFAULT_INSTRUCTION_SPECIAL  = "";
    private static final int    DEFAULT_PRIX_TOTAL           = 0;

    // Validate payment method
    private boolean isValidMethodePaiement(String methode) {
        return ALLOWED_PAIEMENT.contains(methode);
    }

    // Get allowed payment methods for UI
    public static List<String> getAllowedPaymentMethods() {
        return ALLOWED_PAIEMENT;
    }

    @Override
    public boolean ajouter(Commande commande) throws SQLException {
        if (!isValidMethodePaiement(commande.getMethodePaiement())) {
            throw new IllegalArgumentException("Méthode de paiement non valide");
        }
        String sql = "INSERT INTO commande (user_id, client_name, client_family_name, client_adresse, "
                + "client_phone, annonce_quantities, methode_paiement, etat_commande, date, instruction_speciale, prixtotal) "
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
            pst.setInt(11, (int) commande.getPrixTotal());
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public boolean modifier(Commande commande) throws SQLException {
        if (!isValidMethodePaiement(commande.getMethodePaiement())) {
            throw new IllegalArgumentException("Méthode de paiement non valide");
        }
        String sql = "UPDATE commande SET user_id = ?, client_name = ?, client_family_name = ?, "
                + "client_adresse = ?, client_phone = ?, annonce_quantities = ?, methode_paiement = ?, "
                + "etat_commande = ?, date = ?, instruction_speciale = ?, prixtotal = ? WHERE id = ?";
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
            pst.setInt(11, (int) commande.getPrixTotal());
            pst.setInt(12, commande.getId());
            return pst.executeUpdate() > 0;
        }
    }

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
        List<Commande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCommande(rs));
            }
        }
        return list;
    }

    public List<Commande> getByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM commande WHERE user_id = ?";
        List<Commande> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCommande(rs));
                }
            }
        }
        return list;
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande c = new Commande();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setClientName(rs.getString("client_name"));
        c.setClientFamilyName(rs.getString("client_family_name"));
        c.setClientAddress(rs.getString("client_adresse"));
        c.setClientPhone(rs.getString("client_phone"));
        c.setAnnonceQuantities(rs.getString("annonce_quantities"));
        c.setMethodePaiement(rs.getString("methode_paiement"));
        c.setEtatCommande(rs.getString("etat_commande"));
        c.setDate(rs.getTimestamp("date").toLocalDateTime());
        c.setInstructionSpeciale(rs.getString("instruction_speciale"));
        c.setPrixTotal(rs.getDouble("prixtotal"));
        return c;
    }

    /**
     * Sum of all quantities in the user's cart JSON
     */
    public int getTotalQuantityByUser(int userId) throws SQLException {
        String sql = "SELECT annonce_quantities FROM commande WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return 0;
                JSONObject obj = new JSONObject(rs.getString("annonce_quantities"));
                return obj.keySet()
                        .stream()
                        .mapToInt(k -> obj.getInt(k))
                        .sum();
            }
        }
    }

    /**
     * Adds one unit of the specified annonce to the cart (inserting or updating)
     */
    public void addOrUpdateCommande(int userId,
                                    int annonceId,
                                    int quantityToAdd) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            // fetch existing cart row (if any)
            String select = "SELECT id, annonce_quantities FROM commande WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // update existing JSON
                        int cmdId = rs.getInt("id");
                        JSONObject obj = new JSONObject(rs.getString("annonce_quantities"));
                        String key = String.valueOf(annonceId);
                        obj.put(key, obj.optInt(key, 0) + quantityToAdd);

                        String upd = "UPDATE commande SET annonce_quantities = ? WHERE id = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(upd)) {
                            ps2.setString(1, obj.toString());
                            ps2.setInt(2, cmdId);
                            ps2.executeUpdate();
                        }
                    } else {
                        // insert new cart row with defaults
                        JSONObject obj = new JSONObject();
                        obj.put(String.valueOf(annonceId), quantityToAdd);
                        String ins = "INSERT INTO commande (user_id, date, client_name, client_family_name, "
                                + "client_adresse, client_phone, annonce_quantities, methode_paiement, "
                                + "etat_commande, prixtotal, instruction_speciale) VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps2 = conn.prepareStatement(ins)) {
                            ps2.setInt(1, userId);
                            ps2.setString(2, DEFAULT_CLIENT_NAME);
                            ps2.setString(3, DEFAULT_CLIENT_FAMILY_NAME);
                            ps2.setString(4, DEFAULT_CLIENT_ADDRESS);
                            ps2.setString(5, DEFAULT_CLIENT_PHONE);
                            ps2.setString(6, obj.toString());
                            ps2.setString(7, DEFAULT_PAIEMENT_METHOD);
                            ps2.setString(8, DEFAULT_ORDER_STATE);
                            ps2.setInt(9, DEFAULT_PRIX_TOTAL);
                            ps2.setString(10, DEFAULT_INSTRUCTION_SPECIAL);
                            ps2.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
        }
    }
}

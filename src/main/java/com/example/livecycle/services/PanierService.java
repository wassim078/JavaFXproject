package com.example.livecycle.services;

import com.example.livecycle.entities.Panier;
import com.example.livecycle.utils.DatabaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class PanierService {
    public void addItem(int userId, int annonceId, int quantity) throws SQLException {
        int currentQty = getItemQuantity(userId, annonceId);
        JSONObject items = new JSONObject();

        Panier panier = getByUserId(userId);
        if (panier != null && panier.getItems() != null) {
            items = new JSONObject(panier.getItems());
        }

        items.put(String.valueOf(annonceId), currentQty + quantity);
        updatePanierItems(userId, items.toString());
    }

    public Panier getByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM panier WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Panier panier = new Panier();
                    panier.setId(rs.getInt("id"));
                    panier.setUserId(userId);
                    String items = rs.getString("items");
                    panier.setItems(fixJsonArrays(items));
                    return panier;
                }
            }
        }
        return null;
    }

    public void clearPanier(int userId) throws SQLException {
        String query = "DELETE FROM panier WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }

    public void updatePanierItems(int userId, String itemsJson) throws SQLException {
        String query = "INSERT INTO panier (user_id, items) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE items = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, userId);
            pst.setString(2, itemsJson);
            pst.setString(3, itemsJson);
            pst.executeUpdate();
        }
    }

    private String fixJsonArrays(String json) {
        if (json == null || json.isEmpty()) return "{}";
        JSONObject cleaned = new JSONObject(json);

        for (String key : cleaned.keySet()) {
            Object value = cleaned.get(key);
            if (value instanceof JSONArray) {
                JSONArray arr = (JSONArray) value;
                cleaned.put(key, arr.optInt(0, 1));
            }
        }
        return cleaned.toString();
    }

    public int getItemQuantity(int userId, int annonceId) throws SQLException {
        Panier panier = getByUserId(userId);
        if (panier == null || panier.getItems() == null) return 0;

        JSONObject items = new JSONObject(panier.getItems());
        return items.optInt(String.valueOf(annonceId), 0);
    }



}
package com.example.livecycle.services;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieForumService {
    private final Connection connection;

    public CategorieForumService() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<CategoryForum> getAllCategories() throws SQLException {
        List<CategoryForum> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie_forum";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new CategoryForum(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        }
        return categories;
    }

    public void addCategory(CategoryForum category) throws SQLException {
        String query = "INSERT INTO categorie_forum (name, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.executeUpdate();
        }
    }

    public void updateCategory(CategoryForum category) throws SQLException {
        String query = "UPDATE categorie_forum SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteCategory(int id) throws SQLException {
        String query = "DELETE FROM categorie_forum WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
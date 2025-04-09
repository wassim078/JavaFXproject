package com.example.livecycle.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // DatabaseConnection.java

        private static DatabaseConnection instance;
        private Connection connection;

        private DatabaseConnection() {
            try {
                String url = "jdbc:mysql://localhost:3306/livecycle";
                String user = "root"; // Remplacez par vos identifiants
                String password = "";
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static DatabaseConnection getInstance() {
            if (instance == null) {
                instance = new DatabaseConnection();
            }
            return instance;
        }

    public Connection getConnection() throws SQLException {
        // Vérifie si la connexion est toujours valide
        if(connection == null || connection.isClosed() || !connection.isValid(2)) {
            reconnect();
        }
        return connection;
    }

    private void reconnect() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/livecycle",
                "root",
                ""
        );
    }
}


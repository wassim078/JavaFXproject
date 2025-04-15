package com.example.livecycle.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnexion {
    static String user = "root";
    static String password = "";
    static String url = "jdbc:mysql://localhost:3306/livecycle";
    static String driver = "com.mysql.cj.jdbc.Driver";

    public static Connection getCon() {
        Connection con = null;
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie !");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}

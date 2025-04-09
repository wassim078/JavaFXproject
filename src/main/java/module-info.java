module com.example.livecycle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jbcrypt;
    requires com.google.gson;
    requires java.sql;
    requires mysql.connector.j;

    opens com.example.livecycle to javafx.fxml;
    opens com.example.livecycle.models to javafx.base;
    opens com.example.livecycle.controllers to javafx.fxml;
    exports com.example.livecycle;
    exports com.example.livecycle.controllers;
    exports com.example.livecycle.models;
}
module com.example.livecycle {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.desktop;
    requires jbcrypt;
    requires com.google.gson;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.httpserver;
    requires java.net.http;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.json;


    opens com.example.livecycle to javafx.fxml;

    exports com.example.livecycle;
    exports com.example.livecycle.entities;
    exports com.example.livecycle.controllers.backoffice;
    opens com.example.livecycle.controllers.backoffice to javafx.fxml;
    exports com.example.livecycle.controllers.frontoffice;
    opens com.example.livecycle.controllers.frontoffice to javafx.fxml;
    exports com.example.livecycle.controllers.auth;
    opens com.example.livecycle.controllers.auth to javafx.fxml;
    exports com.example.livecycle.services;
    opens com.example.livecycle.entities to javafx.base, javafx.fxml;
    exports com.example.livecycle.utils;
    opens com.example.livecycle.utils to javafx.fxml;
}
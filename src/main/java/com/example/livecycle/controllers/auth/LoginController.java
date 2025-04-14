package com.example.livecycle.controllers.auth;

import com.example.livecycle.Main;
import com.example.livecycle.controllers.backoffice.AdminDashboardController;
import com.example.livecycle.controllers.frontoffice.UserDashboardController;
import com.example.livecycle.services.UserService;
import com.example.livecycle.entities.User;
import com.example.livecycle.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LoginController implements Main.HostServicesAware {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    private final UserService userService = new UserService();

    // GOOGLE AUTHENTICATION
    private final Gson gson = new Gson();
    private static final String CLIENT_ID = "";
    private static final String REDIRECT_URI = "http://localhost:8081/callback";
    private static final String SCOPE = "email profile";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String CLIENT_SECRET = "";
    private HostServices hostServices;
    private static HttpServer callbackServer;
    private static Timer timeoutTimer;
        //GGOGLE AUTHENTICATION
        @Override
        public void setHostServices(HostServices hostServices) {
            this.hostServices = hostServices;

        }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }















    @FXML
    private void handleLogin() throws IOException {
        clearErrors();

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        Map<String, String> errors = ValidationUtils.validateLogin(email, password);

        if (!errors.isEmpty()) {
            emailError.setText(errors.getOrDefault("email", ""));
            passwordError.setText(errors.getOrDefault("password", ""));
            return;
        }

        User user = userService.authenticateUser(email, password);

        if (user != null) {
            redirectBasedOnRole(user);
        } else {
            passwordError.setText("Invalid email or password");
        }
    }

    private void redirectBasedOnRole(User user) throws IOException {
        // Parse JSON roles array
        LoginController.stopCallbackServer();
        List<String> roles = gson.fromJson(user.getRoles(), new TypeToken<List<String>>(){}.getType());

        String fxmlPath;
        if (roles.contains("ROLE_ADMIN")) {
            fxmlPath = "/com/example/livecycle/backoffice/admin_dashboard.fxml";
        } else {
            fxmlPath = "/com/example/livecycle/frontoffice/Home.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Initialize the correct controller based on role
        if (roles.contains("ROLE_ADMIN")) {
            AdminDashboardController controller = loader.getController();
            controller.initData(user);
        } else {
            UserDashboardController controller = loader.getController();
            controller.initData(user);  // Make sure this method exists in UserDashboardController
        }

        Stage stage = (Stage) emailField.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("LiveCycle Dashboard");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    private void clearErrors() {
        emailError.setText("");
        passwordError.setText("");
    }


    @FXML
    private void handleRegisterNavigation(javafx.event.ActionEvent event) {
        try {
            Hyperlink source = (Hyperlink) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/livecycle/auth/register.fxml"));
            Scene scene = new Scene(root, 700, 600); // Set width and height here
            stage.setScene(scene);
            stage.setTitle("LiveCycle Dashboard");

            // Optional: Set minimum size constraints
            stage.setMinWidth(700);
            stage.setMinHeight(600);

            // Optional: Center window on screen
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load registration form");
            alert.showAndWait();
        }
    }



// GOOGLE AUTHENTICATION
@FXML
private void handleGoogleLogin() {
    try {
        if (hostServices == null) {
            hostServices = Main.getAppHostServices();
        }

        String authUrl = String.format(
                "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&prompt=select_account",
                URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8),
                URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8),
                URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
        );

        hostServices.showDocument(authUrl);
        startCallbackServer();
    } catch (Exception e) {
        showAlert("Error", "Failed to initiate Google login: " + e.getMessage());
    }
}

    private synchronized void startCallbackServer() {
        new Thread(() -> {
            try {
                stopCallbackServer(); // Ensure clean state before starting

                callbackServer = HttpServer.create(new InetSocketAddress(8081), 0);
                callbackServer.createContext("/callback", this::handleCallback);
                callbackServer.start();
                resetTimeoutTimer();

            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Server Error",
                        "Could not start callback server: " + e.getMessage()));
            }
        }).start();
    }
    private void handleCallback(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String code = params.get("code");

            String accessToken = getAccessToken(code);
            User googleUser = getGoogleUserInfo(accessToken);

            Platform.runLater(() -> {
                handleGoogleUser(googleUser);
                stopCallbackServer(); // Stop server after successful login
            });

            sendResponse(exchange, 200, "Login successful! You can close this window.");
            resetTimeoutTimer();

        } catch (Exception e) {
            sendResponse(exchange, 500, "Error processing login: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private synchronized static void resetTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopCallbackServer();
            }
        }, 300_000); // 5-minute timeout
    }
    public static synchronized void stopCallbackServer() {
        if (callbackServer != null) {
            callbackServer.stop(0);
            callbackServer = null;
        }
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange,
                              int statusCode, String message) throws IOException {
        byte[] response = message.getBytes();
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
    private String getAccessToken(String code) throws IOException {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, CLIENT_ID,CLIENT_SECRET, REDIRECT_URI
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = new Gson().fromJson(response.body(), JsonObject.class);
            return json.get("access_token").getAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            throw new IOException("Request interrupted", e);
        }
    }

    private User getGoogleUserInfo(String accessToken) throws IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject userInfo = new Gson().fromJson(response.body(), JsonObject.class);

            String givenName = userInfo.has("given_name") ? userInfo.get("given_name").getAsString() : "";
            String familyName = userInfo.has("family_name") ? userInfo.get("family_name").getAsString() : "";
            String email = userInfo.has("email") ? userInfo.get("email").getAsString() : "";
            String picture = userInfo.has("picture") ?
                    userInfo.get("picture").getAsString().replace("s96-c", "s400-c") : "";

            return new User(
                    givenName,
                    familyName,
                    email,
                    "google-auth", // Dummy password
                    "",
                    "",
                    "[\"ROLE_USER\"]",
                    picture
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }





    private void handleGoogleUser(User googleUser) {
        try {
            if (!userService.emailExists(googleUser.getEmail())) {
                userService.ajouter(googleUser);
            }

            User authenticatedUser = userService.authenticateGoogleUser(googleUser.getEmail());
            redirectBasedOnRole(authenticatedUser);
        } catch (Exception e) {
            showAlert("Error", "Failed to process Google login");
        }
    }














}
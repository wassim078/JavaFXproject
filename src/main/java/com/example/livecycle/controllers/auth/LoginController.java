package com.example.livecycle.controllers.auth;

import com.example.livecycle.Main;
import com.example.livecycle.controllers.backoffice.AdminDashboardController;
import com.example.livecycle.controllers.frontoffice.EditProfileController;
import com.example.livecycle.controllers.frontoffice.UserDashboardController;
import com.example.livecycle.services.UserService;
import com.example.livecycle.entities.User;
import com.example.livecycle.utils.ValidationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements Main.HostServicesAware, Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private WebView captchaWebView;
    @FXML private Label captchaError;
    @FXML private Button loginButton;

    private String captchaToken;
    private static final String RECAPTCHA_SITE_KEY = System.getenv("RECAPTCHA_SITE_KEY");
    private static final String RECAPTCHA_SECRET_KEY = System.getenv("RECAPTCHA_SECRET_KEY");
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static HttpServer staticContentServer;

    private final UserService userService = new UserService();

    // GOOGLE AUTHENTICATION
    private final Gson gson = new Gson();
    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String REDIRECT_URI = "http://localhost:8081/callback";
    private static final String SCOPE = "email profile";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    private HostServices hostServices;
    private static HttpServer callbackServer;
    private static HttpServer verificationServer;
    private static Timer timeoutTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        monitorButtonState();
        startStaticContentServer();
        setupRecaptcha();
        startVerificationServer();
    }

    private void startVerificationServer() {
        try {
            verificationServer = HttpServer.create(new InetSocketAddress(8082), 0);
            verificationServer.createContext("/verify", this::handleVerification);
            verificationServer.createContext("/reset", this::handlePasswordReset);
            verificationServer.start();
        } catch (IOException e) {
            System.err.println("Failed to start verification server: " + e.getMessage());
        }
    }


    private void startStaticContentServer() {
        try {
            staticContentServer = HttpServer.create(new InetSocketAddress(8085), 0);
            staticContentServer.createContext("/recaptcha.html", exchange -> {
                InputStream is = getClass().getResourceAsStream("/com/example/livecycle/html/recaptcha.html");
                if (is == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] content = is.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            });
            staticContentServer.start();
        } catch (IOException ex) {
            System.err.println("Could not start static content server: " + ex.getMessage());
        }
    }

    private void handleVerification(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String token = params.get("token");

            if (userService.verifyUser(token)) {
                User verifiedUser = userService.getUserByVerificationToken(token);
                if (verifiedUser != null) {
                    // Force reload all user data
                    verifiedUser = userService.getUser(verifiedUser.getId());
                    notifyEditProfileController(verifiedUser.getId());
                }
                sendResponse(exchange, 200, "Email verified successfully! You can now login.");
                Platform.runLater(() ->
                        showAlertInfo("Verification Success",
                                "Email verified! You can now login with your credentials.")
                );
            } else {
                sendResponse(exchange, 400, "Invalid or expired verification token.");
            }
        } finally {
            exchange.close();
        }
    }



    private void handlePasswordReset(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String token = params.get("token");

            if (userService.isValidResetToken(token)) {
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/reset_password.fxml"));
                        Parent root = loader.load();
                        ResetPasswordController controller = loader.getController();
                        controller.setResetToken(token);

                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Reset Password");
                        stage.show();
                    } catch (IOException e) {
                        System.err.println("Error loading reset password form: " + e.getMessage());
                    }
                });
                sendResponse(exchange, 200, "Valid reset token. Please check the application window.");
            } else {
                sendResponse(exchange, 400, "Invalid or expired reset token");
            }
        } finally {
            exchange.close();
        }
    }






    private void notifyEditProfileController(int userId) {
        EditProfileController controller = EditProfileController.openInstances.get(userId);
        if (controller != null) {
            controller.refreshUserData();
        }
    }


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
        if(captchaToken == null || captchaToken.isEmpty()) {
            captchaError.setText("Please complete the CAPTCHA");
            captchaError.setVisible(true);
            return;
        }

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
            if (!user.isEnabled()) {
                showAlertInfo("Account Not Verified",
                        "Please verify your email first. Check your inbox.");
            }
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

    private void showAlertInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
            boolean isNewUser = !userService.emailExists(googleUser.getEmail());
            if (isNewUser) {
                userService.createGoogleUser(googleUser); // Creates user with enabled=true
            } else {
                userService.enableUser(googleUser.getEmail()); // Ensures existing users are enabled
            }

            User authenticatedUser = userService.authenticateGoogleUser(googleUser.getEmail());
            if (authenticatedUser != null) {
                authenticatedUser.setEnabled(true); // Ensure local object reflects the enabled status
                redirectBasedOnRole(authenticatedUser);
            } else {
                showAlert("Error", "Failed to authenticate after Google login");
            }
        } catch (Exception e) {
            showAlertInfo("Error", "Failed to process Google login: " + e.getMessage());
        }
    }


    @FXML
    private void handleForgotPasswordNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/auth/forgot_password.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }



//RECAPTCHA




    private void monitorButtonState() {
        loginButton.disabledProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("[DEBUG] Login button state changed to: " + (newVal ? "disabled" : "enabled"));
        });
    }


    private void setupRecaptcha() {
        try {

            WebEngine webEngine = captchaWebView.getEngine();

            // Add error handling
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", new JavaConnector());
                    webEngine.executeScript("console.log = function(message) { javaConnector.log(message); };");
                }
            });

            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaConnector", new JavaConnector());

            // Load the recaptcha HTML page (ensure this file has your proper site-key in it)
            String captchaUrl = "http://localhost:8085/recaptcha.html";
            webEngine.load(captchaUrl);

        } catch (Exception e) {
            System.err.println("CAPTCHA initialization failed: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                captchaError.setText("CAPTCHA service unavailable");
                captchaError.setVisible(true);
            });
        }
    }

    public class JavaConnector {
        public void onCaptchaSuccess(String token) {
            System.out.println("[DEBUG] CAPTCHA callback received with token: " + token);
            // Perform verification in a new thread to avoid UI blocking
            new Thread(() -> {
                boolean verified = verifyCaptcha(token);
                Platform.runLater(() -> {
                    if (verified) {
                        System.out.println("[DEBUG] CAPTCHA verified successfully");
                        captchaError.setVisible(false);
                        loginButton.setDisable(false);
                        captchaToken = token;
                        // Optional: Inform the user or update UI as needed
                    } else {
                        System.out.println("[DEBUG] CAPTCHA verification failed");
                        captchaError.setText("CAPTCHA verification failed");
                        captchaError.setVisible(true);
                        captchaToken = null;
                    }
                });
            }).start();
        }
    }


    private boolean verifyCaptcha(String token) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String params = "secret=" + RECAPTCHA_SECRET_KEY + "&response=" + token;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERIFY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Add debug logging
            System.out.println("reCAPTCHA Response: " + response.body());

            JsonObject json = new Gson().fromJson(response.body(), JsonObject.class);

            if (!json.has("success")) {
                System.err.println("Invalid reCAPTCHA response format");
                return false;
            }

            boolean success = json.get("success").getAsBoolean();

            // Check for error codes
            if (json.has("error-codes")) {
                JsonArray errors = json.getAsJsonArray("error-codes");
                errors.forEach(e -> System.err.println("reCAPTCHA Error: " + e.getAsString()));
            }

            return success;

        } catch (Exception e) {
            System.err.println("reCAPTCHA Verification Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }








}
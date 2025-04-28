package com.example.livecycle;

import com.example.livecycle.controllers.auth.LoginController;
import com.example.livecycle.controllers.backoffice.AdminDashboardController;
import com.example.livecycle.controllers.frontoffice.UserDashboardController;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.UserService;
import com.example.livecycle.utils.SessionManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static HostServices hostServices;
private LoginController loginController;

    @Override
    public void start(Stage stage) throws IOException {
        hostServices = getHostServices();

        // Check for existing valid session
        if (SessionManager.isSessionValid()) {
            int userId = SessionManager.getCurrentUserId();
            User user = new UserService().getUser(userId);

            if (user != null && !user.isBanned()) {
                redirectToDashboard(user, stage);
                return;
            } else {
                SessionManager.clearSession();
            }
        }

        // No valid session - show login
        showLoginScreen(stage);
    }

    private void redirectToDashboard(User user, Stage stage) throws IOException {
        String fxmlPath = user.getRoles().contains("ROLE_ADMIN")
                ? "/com/example/livecycle/backoffice/admin_dashboard.fxml"
                : "/com/example/livecycle/frontoffice/Home.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        if (user.getRoles().contains("ROLE_ADMIN")) {
            AdminDashboardController controller = loader.getController();
            controller.initData(user);
        } else {
            UserDashboardController controller = loader.getController();
            controller.initData(user);
        }

        setupStage(stage, root, "LiveCycle Dashboard");
    }


    private void setupStage(Stage stage, Parent root, String title) {
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/logo.png")));
        stage.centerOnScreen();
        stage.show();
    }



    private void showLoginScreen(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/livecycle/auth/login.fxml"));
        Parent root = fxmlLoader.load();
        setupStage(stage, root, "Login");


        this.loginController = fxmlLoader.getController();
        // Set up host services
        Object controller = fxmlLoader.getController();
        if (controller instanceof HostServicesAware) {
            ((HostServicesAware) controller).setHostServices(getHostServices());
        }

        stage.setOnCloseRequest(event -> {
            if (loginController != null) {
                loginController.stopCallbackServer();
            }
        });
    }












    public static HostServices getAppHostServices() {
        return hostServices;
    }



    public static void main(String[] args) {
        launch();
    }

    public interface HostServicesAware {
        void setHostServices(HostServices hostServices);
    }
}



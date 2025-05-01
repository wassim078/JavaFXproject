package com.example.livecycle;

import com.example.livecycle.controllers.auth.LoginController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
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

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/livecycle/auth/register.fxml"));

        Scene scene = new Scene(fxmlLoader.load(),700, 600);

        Object controller = fxmlLoader.getController();
        if (controller instanceof HostServicesAware) {
            ((HostServicesAware) controller).setHostServices(getHostServices());
        }

        stage.setOnCloseRequest(event -> {
            if (loginController != null) {
                loginController.stopCallbackServer();
            }
        });
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/livecycle/images/logo.png")));
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
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



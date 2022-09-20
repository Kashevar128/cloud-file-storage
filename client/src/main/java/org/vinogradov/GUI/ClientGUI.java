package org.vinogradov.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.vinogradov.controllers.ClientController;

import java.io.IOException;

/**
 * JavaFX App
 */
public class ClientGUI extends Application {

    @Override
    public void start(Stage stage) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/vinogradov/fxml/clientWindow.fxml"));
            Parent root = loader.load();
            ClientController clientController = loader.getController();
            stage.setTitle("Java File Storage");
            stage.setScene(new Scene(root, 1000, 600));
//            stage.setOnCloseRequest(windowEvent -> {
//                clientController.nettyClient.exitClient();
//            });

            stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
package org.vinogradov.myclient.GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.vinogradov.myclient.clientService.ClientLogic;
import org.vinogradov.myclient.controllers.ClientController;

import java.io.IOException;

/**
 * JavaFX App
 */
public class ClientGUI {

    private ClientController clientController;

    private Stage stage;

    public ClientGUI(ClientLogic clientLogic) {
        try {
            stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/vinogradov/fxml/clientWindow.fxml"));
            Parent root = loader.load();
            clientController = loader.getController();
            stage.setTitle("Java File Storage");
            stage.setScene(new Scene(root, 1000, 600));
            stage.setOnCloseRequest(windowEvent -> {
               clientLogic.closeClient();
            });
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientController getClientController() {
        return clientController;
    }

    public Stage getStage() {
        return stage;
    }
}
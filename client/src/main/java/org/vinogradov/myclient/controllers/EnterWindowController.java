package org.vinogradov.myclient.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.vinogradov.myclient.GUI.AlertWindowsClass;
import org.vinogradov.mysupport.HelperMethods;


public class EnterWindowController {

    ClientController clientController;

    Stage stage;

    @FXML
    public TextField nameFolderField;

    @FXML
    public void createFolder(ActionEvent actionEvent) {
        String nameFolder = nameFolderField.getText();
        String nameFolderDelSpace = HelperMethods.delSpace(nameFolder);
        if (!filter(nameFolderDelSpace)) return;
        PanelController panelController = clientController.getSrcPC();
        panelController.createNewPackage(nameFolderDelSpace);
        stage.close();
    }

    private boolean filter (String nameFolder) {
        if (nameFolder.isBlank()) {
            return false;
        }
        if (nameFolder.length() > 50){
            AlertWindowsClass.showLengthFolderNameAlert();
            nameFolderField.clear();
            return false;
        }

        return true;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
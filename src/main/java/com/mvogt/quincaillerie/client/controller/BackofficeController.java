package com.mvogt.quincaillerie.client.controller;

import java.io.IOException;

import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class BackofficeController {

    @FXML
    private Label sessionLabel;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab categoriesTab;
    @FXML
    private Tab utilisateursTab;
    @FXML
    private Tab rapportsTab;
    @FXML
    private DashboardTabController dashboardController;

    @FXML
    public void initialize() {
        String role = Session.getInstance().getRole();
        sessionLabel.setText(Session.getInstance().getLogin() + " (" + role + ")");

        if (!"ADMIN".equals(role)) {
            tabPane.getTabs().removeAll(categoriesTab, utilisateursTab, rapportsTab);
        }
    }

    @FXML
    private void handleDeconnexion() {
        try {
            dashboardController.detenerActualisation();
            Session.getInstance().reinitialiser();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sessionLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quincaillerie Mvogt");
        } catch (IOException e) {
            sessionLabel.setText("Erreur navigation : " + e.getMessage());
        }
    }
}

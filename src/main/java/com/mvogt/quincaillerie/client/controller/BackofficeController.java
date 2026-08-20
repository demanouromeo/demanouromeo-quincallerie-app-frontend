package com.mvogt.quincaillerie.client.controller;

import java.io.IOException;

import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
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
    private Tab parametresTab;
    @FXML
    private Button pointDeVenteButton;
    @FXML
    private DashboardTabController dashboardController;

    @FXML
    public void initialize() {
        String role = Session.getInstance().getRole();
        sessionLabel.setText(Session.getInstance().getLogin() + " (" + role + ")");

        if (!"ADMIN".equals(role)) {
            tabPane.getTabs().removeAll(categoriesTab, utilisateursTab, rapportsTab, parametresTab);
        }

        // Seuls ADMIN (via ce bouton) et VENDEUR (qui atterrit deja sur vente.fxml comme
        // ecran d'accueil) doivent pouvoir acceder au point de vente — GESTIONNAIRE ne gere
        // que le stock/approvisionnements, pas les transactions de vente.
        boolean peutAccederPointDeVente = "ADMIN".equals(role);
        pointDeVenteButton.setVisible(peutAccederPointDeVente);
        pointDeVenteButton.setManaged(peutAccederPointDeVente);
    }

    @FXML
    private void handlePointDeVente() {
        try {
            dashboardController.detenerActualisation();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/vente.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sessionLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quincaillerie Mvogt — Vente");
        } catch (IOException e) {
            sessionLabel.setText("Erreur navigation : " + e.getMessage());
        }
    }

    @FXML
    private void handleChangerMotDePasse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/changer_mot_de_passe.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(sessionLabel.getScene().getWindow());
            dialog.setTitle("Changer mot de passe");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            sessionLabel.setText("Erreur ouverture ecran : " + e.getMessage());
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

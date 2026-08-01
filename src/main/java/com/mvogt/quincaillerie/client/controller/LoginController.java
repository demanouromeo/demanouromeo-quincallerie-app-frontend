package com.mvogt.quincaillerie.client.controller;

import com.mvogt.quincaillerie.client.model.LoginRequest;
import com.mvogt.quincaillerie.client.model.LoginResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleTogglePassword() {
        boolean afficherEnClair = !passwordVisibleField.isVisible();
        passwordVisibleField.setVisible(afficherEnClair);
        passwordVisibleField.setManaged(afficherEnClair);
        passwordField.setVisible(!afficherEnClair);
        passwordField.setManaged(!afficherEnClair);
        togglePasswordButton.getStyleClass().remove("password-toggle-button-active");
        if (afficherEnClair) {
            togglePasswordButton.getStyleClass().add("password-toggle-button-active");
        }
    }

    @FXML
    private void handleLogin() {
        statusLabel.setText("Connexion en cours...");
        loginButton.setDisable(true);

        String login = loginField.getText();
        try {
            LoginRequest request = new LoginRequest(login, passwordField.getText());
            LoginResponse response = Session.getInstance().getApiClient().post("/auth/login", request, LoginResponse.class);
            Session.getInstance().ouvrir(login, response.token(), response.role());
            ouvrirEcranPourRole(response.role());
        } catch (Exception e) {
            statusLabel.setText("Echec de connexion : " + e.getMessage());
            loginButton.setDisable(false);
        }
    }

    private void ouvrirEcranPourRole(String role) throws Exception {
        boolean backoffice = "GESTIONNAIRE".equals(role) || "ADMIN".equals(role);
        String vue = backoffice ? "../view/backoffice.fxml" : "../view/vente.fxml";
        String titre = backoffice ? "Quincaillerie Mvogt — Gestion" : "Quincaillerie Mvogt — Vente";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(vue));
        Parent root = loader.load();
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(titre);
    }
}

package com.mvogt.quincaillerie.client.controller;

import com.mvogt.quincaillerie.client.model.LoginRequest;
import com.mvogt.quincaillerie.client.model.LoginResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
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
    private Hyperlink motDePasseOublieLink;

    @FXML
    private CheckBox seSouvenirCheckBox;

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
        String login = loginField.getText();
        String motDePasse = passwordField.getText();

        if (login == null || login.isBlank()) {
            statusLabel.setText("L'identifiant est requis.");
            return;
        }
        if (motDePasse == null || motDePasse.isBlank()) {
            statusLabel.setText("Le mot de passe est requis.");
            return;
        }

        statusLabel.setText("Connexion en cours...");
        loginButton.setDisable(true);

        try {
            LoginRequest request = new LoginRequest(login, motDePasse);
            LoginResponse response = Session.getInstance().getApiClient().post("/auth/login", request, LoginResponse.class);
            Session.getInstance().ouvrir(login, response.token(), response.role());
            if (seSouvenirCheckBox.isSelected()) {
                Session.getInstance().sauvegarderSession(login, response.token(), response.role());
            } else {
                Session.getInstance().effacerSessionSauvegardee();
            }
            ouvrirEcranPourRole(response.role());
        } catch (Exception e) {
            statusLabel.setText(messageConvivial(e));
            loginButton.setDisable(false);
        }
    }

    /** Traduit l'exception technique (souvent un corps JSON brut du serveur) en message
     *  comprehensible pour l'utilisateur, sans jamais afficher la reponse du serveur. */
    private String messageConvivial(Exception e) {
        String message = e.getMessage();
        boolean erreurAuthentification = message != null
                && (message.contains("Erreur API (400") || message.contains("Erreur API (401") || message.contains("Erreur API (403"));
        if (erreurAuthentification) {
            return "Identifiant ou mot de passe incorrect.";
        }
        return "Erreur : impossible de contacter le serveur. Veuillez reessayer.";
    }

    @FXML
    private void handleMotDePasseOublie() {
        Alert alert = new Alert(AlertType.INFORMATION,
                "Contactez votre administrateur pour reinitialiser votre mot de passe.");
        alert.setHeaderText(null);
        alert.showAndWait();
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

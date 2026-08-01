package com.mvogt.quincaillerie.client.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Smoke test de l'ecran de connexion : verifie que login.fxml se charge et que le controleur est
 * correctement cable (la regression la plus probable apres un changement de FXML), sans dependre
 * d'un backend reellement demarre. Un serveur injoignable declenche un "connection refused"
 * quasi instantane sur localhost, capture par LoginController.handleLogin comme n'importe quelle
 * autre erreur — le test reste donc rapide et deterministe meme sans backend lance.
 */
class LoginScreenTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = new FXMLLoader(getClass().getResource("/com/mvogt/quincaillerie/client/view/login.fxml")).load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void ecranDeConnexionAfficheLesChampsAttendus() {
        assertNotNull(lookup("#loginField").query());
        assertNotNull(lookup("#passwordField").query());
        assertNotNull(lookup("#loginButton").query());
        assertNotNull(lookup("#loginField").queryAs(TextField.class));
        assertNotNull(lookup("#passwordField").queryAs(PasswordField.class));
        assertNotNull(lookup("#loginButton").queryAs(Button.class));
    }

    @Test
    void tentativeDeConnexionAfficheUnMessageDeStatut() {
        clickOn("#loginField").write("utilisateur.test");
        clickOn("#passwordField").write("mot-de-passe");
        clickOn("#loginButton");

        Label statusLabel = lookup("#statusLabel").queryAs(Label.class);
        assertFalse(statusLabel.getText().isBlank());
    }
}

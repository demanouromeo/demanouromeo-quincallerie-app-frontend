package com.mvogt.quincaillerie.client.controller;

import com.mvogt.quincaillerie.client.model.ChangePasswordRequest;
import com.mvogt.quincaillerie.client.model.MessageResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangerMotDePasseController {

    @FXML
    private PasswordField ancienMotDePasseField;
    @FXML
    private PasswordField nouveauMotDePasseField;
    @FXML
    private PasswordField confirmerMotDePasseField;
    @FXML
    private Label statusLabel;

    @FXML
    private void handleValider() {
        String ancien = ancienMotDePasseField.getText();
        String nouveau = nouveauMotDePasseField.getText();
        String confirmation = confirmerMotDePasseField.getText();

        if (ancien == null || ancien.isBlank() || nouveau == null || nouveau.isBlank()) {
            statusLabel.setText("Tous les champs sont obligatoires.");
            return;
        }
        if (nouveau.length() < 6) {
            statusLabel.setText("Le nouveau mot de passe doit contenir au moins 6 caracteres.");
            return;
        }
        if (!nouveau.equals(confirmation)) {
            statusLabel.setText("La confirmation ne correspond pas au nouveau mot de passe.");
            return;
        }

        try {
            Session.getInstance().getApiClient().put("/compte/mot-de-passe",
                    new ChangePasswordRequest(ancien, nouveau), MessageResponse.class);
            statusLabel.setText("Mot de passe modifie avec succes.");
            ancienMotDePasseField.clear();
            nouveauMotDePasseField.clear();
            confirmerMotDePasseField.clear();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Erreur API (400")) {
                statusLabel.setText("Ancien mot de passe incorrect.");
            } else {
                statusLabel.setText("Erreur : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
}

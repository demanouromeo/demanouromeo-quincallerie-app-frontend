package com.mvogt.quincaillerie.client.controller;

import com.mvogt.quincaillerie.client.model.ParametresRequest;
import com.mvogt.quincaillerie.client.model.ParametresResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ParametresTabController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField domaineField;
    @FXML
    private TextField villeField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        charger();
    }

    private void charger() {
        try {
            ParametresResponse parametres = Session.getInstance().getApiClient()
                    .get("/parametres", ParametresResponse.class);
            afficher(parametres);
            statusLabel.setText("Parametres charges.");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement : " + e.getMessage());
        }
    }

    private void afficher(ParametresResponse parametres) {
        nomField.setText(parametres.nom());
        domaineField.setText(parametres.domaine());
        villeField.setText(parametres.ville());
        telephoneField.setText(parametres.telephone());
        emailField.setText(parametres.email());
    }

    @FXML
    private void handleEnregistrer() {
        if (nomField.getText() == null || nomField.getText().isBlank()) {
            statusLabel.setText("Le nom est obligatoire.");
            return;
        }
        if (domaineField.getText() == null || domaineField.getText().isBlank()) {
            statusLabel.setText("Le domaine d'activite est obligatoire.");
            return;
        }
        if (villeField.getText() == null || villeField.getText().isBlank()) {
            statusLabel.setText("La ville est obligatoire.");
            return;
        }
        if (telephoneField.getText() == null || telephoneField.getText().isBlank()) {
            statusLabel.setText("Le telephone est obligatoire.");
            return;
        }
        if (emailField.getText() == null || emailField.getText().isBlank()) {
            statusLabel.setText("L'email est obligatoire.");
            return;
        }

        ParametresRequest request = new ParametresRequest(
                nomField.getText().trim(),
                domaineField.getText().trim(),
                telephoneField.getText().trim(),
                villeField.getText().trim(),
                emailField.getText().trim());
        try {
            ParametresResponse resultat = Session.getInstance().getApiClient()
                    .put("/parametres", request, ParametresResponse.class);
            afficher(resultat);
            statusLabel.setText("Parametres enregistres.");
        } catch (Exception e) {
            statusLabel.setText("Erreur enregistrement : " + e.getMessage());
        }
    }
}

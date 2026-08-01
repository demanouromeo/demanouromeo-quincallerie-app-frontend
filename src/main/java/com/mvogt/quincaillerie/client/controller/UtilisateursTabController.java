package com.mvogt.quincaillerie.client.controller;

import java.util.List;

import com.mvogt.quincaillerie.client.model.UtilisateurRequest;
import com.mvogt.quincaillerie.client.model.UtilisateurResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UtilisateursTabController {

    @FXML
    private TableView<UtilisateurResponse> utilisateursTable;
    @FXML
    private TableColumn<UtilisateurResponse, String> colNom;
    @FXML
    private TableColumn<UtilisateurResponse, String> colLogin;
    @FXML
    private TableColumn<UtilisateurResponse, String> colRole;
    @FXML
    private TableColumn<UtilisateurResponse, Boolean> colActif;
    @FXML
    private TextField nomField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField motDePasseField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private CheckBox actifCheck;
    @FXML
    private Label statusLabel;

    private final ObservableList<UtilisateurResponse> utilisateurs = FXCollections.observableArrayList();
    private UtilisateurResponse selection;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        utilisateursTable.setItems(utilisateurs);
        utilisateursTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                remplirFormulaire(nouveau);
            }
        });

        roleCombo.setItems(FXCollections.observableArrayList("VENDEUR", "GESTIONNAIRE", "ADMIN"));

        charger();
    }

    private void charger() {
        try {
            List<UtilisateurResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/utilisateurs", UtilisateurResponse.class);
            utilisateurs.setAll(resultats);
            statusLabel.setText(resultats.size() + " utilisateur(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement : " + e.getMessage());
        }
    }

    private void remplirFormulaire(UtilisateurResponse utilisateur) {
        selection = utilisateur;
        nomField.setText(utilisateur.nom());
        loginField.setText(utilisateur.login());
        motDePasseField.clear();
        roleCombo.getSelectionModel().select(utilisateur.role());
        actifCheck.setSelected(utilisateur.actif());
    }

    @FXML
    private void handleNouveau() {
        selection = null;
        utilisateursTable.getSelectionModel().clearSelection();
        nomField.clear();
        loginField.clear();
        motDePasseField.clear();
        roleCombo.getSelectionModel().clearSelection();
        actifCheck.setSelected(true);
    }

    @FXML
    private void handleEnregistrer() {
        if (nomField.getText() == null || nomField.getText().isBlank()
                || loginField.getText() == null || loginField.getText().isBlank()
                || roleCombo.getSelectionModel().getSelectedItem() == null) {
            statusLabel.setText("Nom, login et role sont obligatoires.");
            return;
        }
        UtilisateurRequest request = new UtilisateurRequest(
                nomField.getText().trim(),
                loginField.getText().trim(),
                motDePasseField.getText(),
                roleCombo.getSelectionModel().getSelectedItem(),
                actifCheck.isSelected());
        try {
            if (selection == null) {
                Session.getInstance().getApiClient().post("/utilisateurs", request, UtilisateurResponse.class);
                statusLabel.setText("Utilisateur cree.");
            } else {
                Session.getInstance().getApiClient()
                        .put("/utilisateurs/" + selection.id(), request, UtilisateurResponse.class);
                statusLabel.setText("Utilisateur modifie.");
            }
            handleNouveau();
            charger();
        } catch (Exception e) {
            statusLabel.setText("Erreur enregistrement : " + e.getMessage());
        }
    }
}

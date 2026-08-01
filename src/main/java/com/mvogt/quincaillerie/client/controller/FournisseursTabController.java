package com.mvogt.quincaillerie.client.controller;

import java.util.List;

import com.mvogt.quincaillerie.client.model.FournisseurRequest;
import com.mvogt.quincaillerie.client.model.FournisseurResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class FournisseursTabController {

    @FXML
    private TableView<FournisseurResponse> fournisseursTable;
    @FXML
    private TableColumn<FournisseurResponse, String> colNom;
    @FXML
    private TableColumn<FournisseurResponse, String> colContact;
    @FXML
    private TableColumn<FournisseurResponse, String> colAdresse;
    @FXML
    private TextField nomField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField adresseField;
    @FXML
    private Label statusLabel;

    private final ObservableList<FournisseurResponse> fournisseurs = FXCollections.observableArrayList();
    private FournisseurResponse selection;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        fournisseursTable.setItems(fournisseurs);
        fournisseursTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                selection = nouveau;
                nomField.setText(nouveau.nom());
                contactField.setText(nouveau.contact());
                adresseField.setText(nouveau.adresse());
            }
        });
        charger();
    }

    private void charger() {
        try {
            List<FournisseurResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/fournisseurs", FournisseurResponse.class);
            fournisseurs.setAll(resultats);
            statusLabel.setText(resultats.size() + " fournisseur(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement : " + e.getMessage());
        }
    }

    @FXML
    private void handleNouveau() {
        selection = null;
        fournisseursTable.getSelectionModel().clearSelection();
        nomField.clear();
        contactField.clear();
        adresseField.clear();
    }

    @FXML
    private void handleEnregistrer() {
        if (nomField.getText() == null || nomField.getText().isBlank()) {
            statusLabel.setText("Le nom est obligatoire.");
            return;
        }
        FournisseurRequest request = new FournisseurRequest(
                nomField.getText().trim(), contactField.getText(), adresseField.getText());
        try {
            if (selection == null) {
                Session.getInstance().getApiClient().post("/fournisseurs", request, FournisseurResponse.class);
                statusLabel.setText("Fournisseur cree.");
            } else {
                Session.getInstance().getApiClient()
                        .put("/fournisseurs/" + selection.id(), request, FournisseurResponse.class);
                statusLabel.setText("Fournisseur modifie.");
            }
            handleNouveau();
            charger();
        } catch (Exception e) {
            statusLabel.setText("Erreur enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selection == null) {
            statusLabel.setText("Selectionnez un fournisseur d'abord.");
            return;
        }
        try {
            Session.getInstance().getApiClient().delete("/fournisseurs/" + selection.id());
            statusLabel.setText("Fournisseur supprime.");
            handleNouveau();
            charger();
        } catch (Exception e) {
            statusLabel.setText("Erreur suppression : " + e.getMessage());
        }
    }
}

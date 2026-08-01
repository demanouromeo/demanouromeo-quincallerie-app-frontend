package com.mvogt.quincaillerie.client.controller;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.mvogt.quincaillerie.client.model.ApprovisionnementRequest;
import com.mvogt.quincaillerie.client.model.ApprovisionnementResponse;
import com.mvogt.quincaillerie.client.model.FournisseurResponse;
import com.mvogt.quincaillerie.client.model.LigneApproSaisie;
import com.mvogt.quincaillerie.client.model.LigneApprovisionnementRequest;
import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class ApprovisionnementsTabController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    private ComboBox<FournisseurResponse> fournisseurCombo;
    @FXML
    private ComboBox<ProduitResponse> produitCombo;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField prixAchatUnitaireField;
    @FXML
    private TableView<LigneApproSaisie> lignesTable;
    @FXML
    private TableColumn<LigneApproSaisie, String> colLigneProduit;
    @FXML
    private TableColumn<LigneApproSaisie, Integer> colLigneQuantite;
    @FXML
    private TableColumn<LigneApproSaisie, BigDecimal> colLignePrixUnitaire;
    @FXML
    private TableColumn<LigneApproSaisie, BigDecimal> colLigneSousTotal;
    @FXML
    private Button validerButton;
    @FXML
    private Label statusLabel;

    @FXML
    private TableView<ApprovisionnementResponse> historiqueTable;
    @FXML
    private TableColumn<ApprovisionnementResponse, Long> colHistoId;
    @FXML
    private TableColumn<ApprovisionnementResponse, String> colHistoDate;
    @FXML
    private TableColumn<ApprovisionnementResponse, String> colHistoFournisseur;
    @FXML
    private TableColumn<ApprovisionnementResponse, String> colHistoGestionnaire;
    @FXML
    private TableColumn<ApprovisionnementResponse, BigDecimal> colHistoMontant;

    private final ObservableList<LigneApproSaisie> lignes = FXCollections.observableArrayList();
    private final ObservableList<ApprovisionnementResponse> historique = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        fournisseurCombo.setConverter(nomConverter(FournisseurResponse::nom));
        produitCombo.setConverter(nomConverter(p -> p.reference() + " - " + p.nom()));

        colLigneProduit.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        colLigneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colLignePrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixAchatUnitaire"));
        colLigneSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        lignesTable.setItems(lignes);

        colHistoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistoDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(DATE_FORMAT.format(data.getValue().dateAppro())));
        colHistoFournisseur.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
        colHistoGestionnaire.setCellValueFactory(new PropertyValueFactory<>("gestionnaireLogin"));
        colHistoMontant.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        historiqueTable.setItems(historique);

        chargerFournisseurs();
        chargerProduits();
        chargerHistorique();
    }

    private <T> StringConverter<T> nomConverter(java.util.function.Function<T, String> libelle) {
        return new StringConverter<>() {
            @Override
            public String toString(T objet) {
                return objet == null ? "" : libelle.apply(objet);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    private void chargerFournisseurs() {
        try {
            List<FournisseurResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/fournisseurs", FournisseurResponse.class);
            fournisseurCombo.setItems(FXCollections.observableArrayList(resultats));
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement fournisseurs : " + e.getMessage());
        }
    }

    private void chargerProduits() {
        try {
            List<ProduitResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/produits", ProduitResponse.class);
            produitCombo.setItems(FXCollections.observableArrayList(resultats));
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement produits : " + e.getMessage());
        }
    }

    private void chargerHistorique() {
        try {
            List<ApprovisionnementResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/approvisionnements", ApprovisionnementResponse.class);
            historique.setAll(resultats);
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement historique : " + e.getMessage());
        }
    }

    @FXML
    private void handleRafraichirHistorique() {
        chargerHistorique();
    }

    @FXML
    private void handleAjouterLigne() {
        ProduitResponse produit = produitCombo.getSelectionModel().getSelectedItem();
        if (produit == null) {
            statusLabel.setText("Selectionnez un produit d'abord.");
            return;
        }
        int quantite;
        BigDecimal prixAchatUnitaire;
        try {
            quantite = Integer.parseInt(quantiteField.getText().trim());
            prixAchatUnitaire = new BigDecimal(prixAchatUnitaireField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Quantite ou prix invalide.");
            return;
        }
        if (quantite <= 0 || prixAchatUnitaire.signum() <= 0) {
            statusLabel.setText("La quantite et le prix doivent etre positifs.");
            return;
        }
        lignes.add(new LigneApproSaisie(produit, quantite, prixAchatUnitaire));
        statusLabel.setText("Ligne ajoutee : " + produit.nom());
    }

    @FXML
    private void handleRetirerLigne() {
        LigneApproSaisie selection = lignesTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            lignes.remove(selection);
        }
    }

    @FXML
    private void handleValider() {
        FournisseurResponse fournisseur = fournisseurCombo.getSelectionModel().getSelectedItem();
        if (fournisseur == null) {
            statusLabel.setText("Selectionnez un fournisseur.");
            return;
        }
        if (lignes.isEmpty()) {
            statusLabel.setText("Ajoutez au moins une ligne.");
            return;
        }

        List<LigneApprovisionnementRequest> lignesRequest = lignes.stream()
                .map(l -> new LigneApprovisionnementRequest(l.getProduit().id(), l.getQuantite(), l.getPrixAchatUnitaire()))
                .toList();

        validerButton.setDisable(true);
        try {
            ApprovisionnementResponse cree = Session.getInstance().getApiClient().post(
                    "/approvisionnements", new ApprovisionnementRequest(fournisseur.id(), lignesRequest),
                    ApprovisionnementResponse.class);
            statusLabel.setText("Approvisionnement #" + cree.id() + " enregistre. Montant : " + cree.montantTotal() + " FCFA.");
            lignes.clear();
            chargerProduits();
            chargerHistorique();
        } catch (Exception e) {
            statusLabel.setText("Echec de l'enregistrement : " + e.getMessage());
        } finally {
            validerButton.setDisable(false);
        }
    }
}

package com.mvogt.quincaillerie.client.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.mvogt.quincaillerie.client.model.LignePanier;
import com.mvogt.quincaillerie.client.model.LigneVenteRequest;
import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.model.VenteRequest;
import com.mvogt.quincaillerie.client.model.VenteResponse;
import com.mvogt.quincaillerie.client.service.ReceiptService;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VenteScreenController {

    @FXML
    private Label sessionLabel;
    @FXML
    private TextField rechercheField;
    @FXML
    private TableView<ProduitResponse> produitsTable;
    @FXML
    private TableColumn<ProduitResponse, String> colReference;
    @FXML
    private TableColumn<ProduitResponse, String> colNom;
    @FXML
    private TableColumn<ProduitResponse, BigDecimal> colPrixVente;
    @FXML
    private TableColumn<ProduitResponse, Integer> colStock;
    @FXML
    private TextField quantiteField;
    @FXML
    private TableView<LignePanier> panierTable;
    @FXML
    private TableColumn<LignePanier, String> colPanierProduit;
    @FXML
    private TableColumn<LignePanier, Integer> colPanierQuantite;
    @FXML
    private TableColumn<LignePanier, BigDecimal> colPanierPrixUnitaire;
    @FXML
    private TableColumn<LignePanier, BigDecimal> colPanierSousTotal;
    @FXML
    private Label totalLabel;
    @FXML
    private Button validerButton;
    @FXML
    private Label statusLabel;

    private final ObservableList<ProduitResponse> produits = FXCollections.observableArrayList();
    private final ObservableList<LignePanier> panier = FXCollections.observableArrayList();
    private final ReceiptService receiptService = new ReceiptService();

    @FXML
    public void initialize() {
        sessionLabel.setText(Session.getInstance().getLogin() + " (" + Session.getInstance().getRole() + ")");

        colReference.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().reference()));
        colNom.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().nom()));
        colPrixVente.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().prixVente()));
        colStock.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().stockActuel()));
        produitsTable.setItems(produits);

        colPanierProduit.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        colPanierQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPanierPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colPanierSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        panierTable.setItems(panier);

        chargerProduits();
    }

    private void chargerProduits() {
        try {
            List<ProduitResponse> resultats = Session.getInstance().getApiClient().getList("/produits", ProduitResponse.class);
            produits.setAll(resultats);
            statusLabel.setText(resultats.size() + " produit(s) charge(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement produits : " + e.getMessage());
        }
    }

    @FXML
    private void handleRecherche() {
        String terme = rechercheField.getText() == null ? "" : rechercheField.getText().trim().toLowerCase();
        try {
            List<ProduitResponse> tous = Session.getInstance().getApiClient().getList("/produits", ProduitResponse.class);
            List<ProduitResponse> filtres = terme.isEmpty()
                    ? tous
                    : tous.stream()
                            .filter(p -> p.reference().toLowerCase().contains(terme) || p.nom().toLowerCase().contains(terme))
                            .toList();
            produits.setAll(filtres);
            statusLabel.setText(filtres.size() + " resultat(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur recherche : " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouterAuPanier() {
        ProduitResponse selection = produitsTable.getSelectionModel().getSelectedItem();
        if (selection == null) {
            statusLabel.setText("Selectionnez un produit d'abord.");
            return;
        }
        int quantite;
        try {
            quantite = Integer.parseInt(quantiteField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Quantite invalide.");
            return;
        }
        if (quantite <= 0) {
            statusLabel.setText("La quantite doit etre positive.");
            return;
        }

        int dejaAuPanier = panier.stream()
                .filter(l -> l.getProduit().id().equals(selection.id()))
                .mapToInt(LignePanier::getQuantite)
                .sum();
        if (dejaAuPanier + quantite > selection.stockActuel()) {
            statusLabel.setText("Stock insuffisant (disponible : " + selection.stockActuel() + ").");
            return;
        }

        int finalQuantite = quantite;
        panier.stream()
                .filter(l -> l.getProduit().id().equals(selection.id()))
                .findFirst()
                .ifPresentOrElse(
                        ligne -> ligne.setQuantite(ligne.getQuantite() + finalQuantite),
                        () -> panier.add(new LignePanier(selection, finalQuantite)));
        panierTable.refresh();
        recalculerTotal();
        statusLabel.setText("Ajoute au panier : " + selection.nom());
    }

    @FXML
    private void handleRetirerLigne() {
        LignePanier selection = panierTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            panier.remove(selection);
            recalculerTotal();
        }
    }

    private void recalculerTotal() {
        BigDecimal total = panier.stream().map(LignePanier::getSousTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total : " + total + " FCFA");
    }

    @FXML
    private void handleValiderVente() {
        if (panier.isEmpty()) {
            statusLabel.setText("Le panier est vide.");
            return;
        }
        List<LigneVenteRequest> lignes = panier.stream()
                .map(l -> new LigneVenteRequest(l.getProduit().id(), l.getQuantite()))
                .toList();
        validerButton.setDisable(true);
        try {
            VenteResponse vente = Session.getInstance().getApiClient()
                    .post("/ventes", new VenteRequest(lignes), VenteResponse.class);
            statusLabel.setText("Vente #" + vente.id() + " enregistree. Total : " + vente.montantTotal() + " FCFA.");
            imprimerRecu(vente);
            panier.clear();
            recalculerTotal();
            chargerProduits();
        } catch (Exception e) {
            statusLabel.setText("Echec de la vente : " + e.getMessage());
        } finally {
            validerButton.setDisable(false);
        }
    }

    private void imprimerRecu(VenteResponse vente) {
        try {
            receiptService.genererEtImprimer(vente, Session.getInstance().getLogin());
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.WARNING,
                    "Vente enregistree mais l'impression du recu a echoue : " + e.getMessage());
            alert.showAndWait();
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
            statusLabel.setText("Erreur ouverture ecran : " + e.getMessage());
        }
    }

    @FXML
    private void handleDeconnexion() {
        try {
            Session.getInstance().reinitialiser();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sessionLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quincaillerie Mvogt");
        } catch (IOException e) {
            statusLabel.setText("Erreur navigation : " + e.getMessage());
        }
    }
}

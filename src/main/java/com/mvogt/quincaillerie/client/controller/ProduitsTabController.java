package com.mvogt.quincaillerie.client.controller;

import java.math.BigDecimal;
import java.util.List;

import com.mvogt.quincaillerie.client.model.CategorieResponse;
import com.mvogt.quincaillerie.client.model.ProduitRequest;
import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class ProduitsTabController {

    @FXML
    private CheckBox alertesSeulementCheck;
    @FXML
    private TableView<ProduitResponse> produitsTable;
    @FXML
    private TableColumn<ProduitResponse, ProduitResponse> colIndex;
    @FXML
    private TableColumn<ProduitResponse, String> colReference;
    @FXML
    private TableColumn<ProduitResponse, String> colNom;
    @FXML
    private TableColumn<ProduitResponse, String> colCategorie;
    @FXML
    private TableColumn<ProduitResponse, String> colUnite;
    @FXML
    private TableColumn<ProduitResponse, BigDecimal> colPrixAchat;
    @FXML
    private TableColumn<ProduitResponse, BigDecimal> colPrixVente;
    @FXML
    private TableColumn<ProduitResponse, Integer> colSeuilAlerte;
    @FXML
    private TableColumn<ProduitResponse, Integer> colStock;

    @FXML
    private TextField referenceField;
    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<CategorieResponse> categorieCombo;
    @FXML
    private TextField uniteField;
    @FXML
    private TextField prixAchatField;
    @FXML
    private TextField prixVenteField;
    @FXML
    private TextField seuilAlerteField;
    @FXML
    private TextField stockActuelField;
    @FXML
    private Button supprimerButton;
    @FXML
    private Label statusLabel;

    private final ObservableList<ProduitResponse> produits = FXCollections.observableArrayList();
    private ProduitResponse produitSelectionne;

    @FXML
    public void initialize() {
        colIndex.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colIndex.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ProduitResponse produit, boolean vide) {
                super.updateItem(produit, vide);
                setText(vide ? null : String.valueOf(getIndex() + 1));
            }
        });
        colReference.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().reference()));
        colNom.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().nom()));
        colCategorie.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().categorieNom()));
        colUnite.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().unite()));
        colPrixAchat.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().prixAchat()));
        colPrixVente.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().prixVente()));
        colSeuilAlerte.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().seuilAlerte()));
        colStock.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().stockActuel()));
        produitsTable.setItems(produits);
        produitsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                remplirFormulaire(nouveau);
            }
        });

        categorieCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategorieResponse categorie) {
                return categorie == null ? "" : categorie.nom();
            }

            @Override
            public CategorieResponse fromString(String string) {
                return null;
            }
        });

        supprimerButton.setDisable(!"ADMIN".equals(Session.getInstance().getRole()));

        chargerCategories();
        chargerProduits();
    }

    private void chargerCategories() {
        try {
            List<CategorieResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/categories", CategorieResponse.class);
            categorieCombo.setItems(FXCollections.observableArrayList(resultats));
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement categories : " + e.getMessage());
        }
    }

    private void chargerProduits() {
        try {
            boolean alertesSeulement = alertesSeulementCheck.isSelected();
            List<ProduitResponse> resultats = Session.getInstance().getApiClient()
                    .getList(alertesSeulement ? "/produits/alertes" : "/produits", ProduitResponse.class);
            produits.setAll(resultats);
            statusLabel.setText(resultats.size() + " produit(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement produits : " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleAlertes() {
        chargerProduits();
    }

    @FXML
    private void handleRafraichir() {
        chargerCategories();
        chargerProduits();
    }

    @FXML
    private void handleNouveau() {
        produitSelectionne = null;
        produitsTable.getSelectionModel().clearSelection();
        referenceField.clear();
        nomField.clear();
        categorieCombo.getSelectionModel().clearSelection();
        uniteField.clear();
        prixAchatField.clear();
        prixVenteField.clear();
        seuilAlerteField.clear();
        stockActuelField.clear();
    }

    private void remplirFormulaire(ProduitResponse produit) {
        produitSelectionne = produit;
        referenceField.setText(produit.reference());
        nomField.setText(produit.nom());
        categorieCombo.getSelectionModel().select(
                categorieCombo.getItems().stream()
                        .filter(c -> c.id().equals(produit.categorieId()))
                        .findFirst().orElse(null));
        uniteField.setText(produit.unite());
        prixAchatField.setText(produit.prixAchat().toString());
        prixVenteField.setText(produit.prixVente().toString());
        seuilAlerteField.setText(String.valueOf(produit.seuilAlerte()));
        stockActuelField.setText(String.valueOf(produit.stockActuel()));
    }

    @FXML
    private void handleEnregistrer() {
        try {
            CategorieResponse categorie = categorieCombo.getSelectionModel().getSelectedItem();
            ProduitRequest request = new ProduitRequest(
                    referenceField.getText().trim(),
                    nomField.getText().trim(),
                    categorie != null ? categorie.id() : null,
                    uniteField.getText().trim(),
                    new BigDecimal(prixAchatField.getText().trim()),
                    new BigDecimal(prixVenteField.getText().trim()),
                    Integer.parseInt(seuilAlerteField.getText().trim()),
                    Integer.parseInt(stockActuelField.getText().trim()));

            if (produitSelectionne == null) {
                ProduitResponse cree = Session.getInstance().getApiClient()
                        .post("/produits", request, ProduitResponse.class);
                statusLabel.setText("Produit cree : " + cree.nom());
            } else {
                ProduitResponse modifie = Session.getInstance().getApiClient()
                        .put("/produits/" + produitSelectionne.id(), request, ProduitResponse.class);
                statusLabel.setText("Produit modifie : " + modifie.nom());
            }
            handleNouveau();
            chargerProduits();
        } catch (NumberFormatException e) {
            statusLabel.setText("Valeur numerique invalide.");
        } catch (Exception e) {
            statusLabel.setText("Erreur enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        if (produitSelectionne == null) {
            statusLabel.setText("Selectionnez un produit d'abord.");
            return;
        }
        try {
            Session.getInstance().getApiClient().delete("/produits/" + produitSelectionne.id());
            statusLabel.setText("Produit supprime.");
            handleNouveau();
            chargerProduits();
        } catch (Exception e) {
            statusLabel.setText("Erreur suppression : " + e.getMessage());
        }
    }
}

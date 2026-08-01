package com.mvogt.quincaillerie.client.controller;

import java.util.List;

import com.mvogt.quincaillerie.client.model.CategorieRequest;
import com.mvogt.quincaillerie.client.model.CategorieResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CategoriesTabController {

    @FXML
    private TableView<CategorieResponse> categoriesTable;
    @FXML
    private TableColumn<CategorieResponse, String> colNom;
    @FXML
    private TextField nomField;
    @FXML
    private Label statusLabel;

    private final ObservableList<CategorieResponse> categories = FXCollections.observableArrayList();
    private CategorieResponse selection;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categoriesTable.setItems(categories);
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                selection = nouveau;
                nomField.setText(nouveau.nom());
            }
        });
        charger();
    }

    private void charger() {
        try {
            List<CategorieResponse> resultats = Session.getInstance().getApiClient()
                    .getList("/categories", CategorieResponse.class);
            categories.setAll(resultats);
            statusLabel.setText(resultats.size() + " categorie(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement : " + e.getMessage());
        }
    }

    @FXML
    private void handleNouveau() {
        selection = null;
        categoriesTable.getSelectionModel().clearSelection();
        nomField.clear();
    }

    @FXML
    private void handleEnregistrer() {
        if (nomField.getText() == null || nomField.getText().isBlank()) {
            statusLabel.setText("Le nom est obligatoire.");
            return;
        }
        CategorieRequest request = new CategorieRequest(nomField.getText().trim());
        try {
            if (selection == null) {
                Session.getInstance().getApiClient().post("/categories", request, CategorieResponse.class);
                statusLabel.setText("Categorie creee.");
            } else {
                Session.getInstance().getApiClient()
                        .put("/categories/" + selection.id(), request, CategorieResponse.class);
                statusLabel.setText("Categorie modifiee.");
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
            statusLabel.setText("Selectionnez une categorie d'abord.");
            return;
        }
        try {
            Session.getInstance().getApiClient().delete("/categories/" + selection.id());
            statusLabel.setText("Categorie supprimee.");
            handleNouveau();
            charger();
        } catch (Exception e) {
            statusLabel.setText("Erreur suppression : " + e.getMessage());
        }
    }
}

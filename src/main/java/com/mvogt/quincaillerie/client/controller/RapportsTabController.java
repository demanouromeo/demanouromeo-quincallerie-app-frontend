package com.mvogt.quincaillerie.client.controller;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mvogt.quincaillerie.client.model.LigneVenteResponse;
import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.model.ProduitVenduDto;
import com.mvogt.quincaillerie.client.model.VenteResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RapportsTabController {

    @FXML
    private Label ventesJourLabel;
    @FXML
    private Label ventesSemaineLabel;
    @FXML
    private Label ventesMoisLabel;
    @FXML
    private Label alertesLabel;
    @FXML
    private TableView<ProduitVenduDto> topProduitsTable;
    @FXML
    private TableColumn<ProduitVenduDto, String> colProduitNom;
    @FXML
    private TableColumn<ProduitVenduDto, Integer> colQuantiteVendue;
    @FXML
    private Label statusLabel;

    private final ObservableList<ProduitVenduDto> topProduits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProduitNom.setCellValueFactory(new PropertyValueFactory<>("produitNom"));
        colQuantiteVendue.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));
        topProduitsTable.setItems(topProduits);
        charger();
    }

    @FXML
    private void handleRafraichir() {
        charger();
    }

    private void charger() {
        try {
            List<VenteResponse> ventes = Session.getInstance().getApiClient().getList("/ventes", VenteResponse.class);
            calculerTotaux(ventes);
            calculerTopProduits(ventes);

            List<ProduitResponse> alertes = Session.getInstance().getApiClient()
                    .getList("/produits/alertes", ProduitResponse.class);
            alertesLabel.setText(String.valueOf(alertes.size()));

            statusLabel.setText("Rapport calcule sur " + ventes.size() + " vente(s).");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement rapports : " + e.getMessage());
        }
    }

    private void calculerTotaux(List<VenteResponse> ventes) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate aujourdHui = LocalDate.now(zone);
        LocalDate debutSemaine = aujourdHui.with(DayOfWeek.MONDAY);
        LocalDate debutMois = aujourdHui.withDayOfMonth(1);

        BigDecimal totalJour = BigDecimal.ZERO;
        BigDecimal totalSemaine = BigDecimal.ZERO;
        BigDecimal totalMois = BigDecimal.ZERO;
        int nbJour = 0;
        int nbSemaine = 0;
        int nbMois = 0;

        for (VenteResponse vente : ventes) {
            LocalDate dateVente = vente.dateVente().atZone(zone).toLocalDate();
            if (!dateVente.isBefore(debutMois)) {
                totalMois = totalMois.add(vente.montantTotal());
                nbMois++;
            }
            if (!dateVente.isBefore(debutSemaine)) {
                totalSemaine = totalSemaine.add(vente.montantTotal());
                nbSemaine++;
            }
            if (dateVente.isEqual(aujourdHui)) {
                totalJour = totalJour.add(vente.montantTotal());
                nbJour++;
            }
        }

        ventesJourLabel.setText(nbJour + " vente(s) — " + totalJour + " FCFA");
        ventesSemaineLabel.setText(nbSemaine + " vente(s) — " + totalSemaine + " FCFA");
        ventesMoisLabel.setText(nbMois + " vente(s) — " + totalMois + " FCFA");
    }

    private void calculerTopProduits(List<VenteResponse> ventes) {
        Map<String, Integer> quantitesParProduit = new LinkedHashMap<>();
        for (VenteResponse vente : ventes) {
            for (LigneVenteResponse ligne : vente.lignes()) {
                quantitesParProduit.merge(ligne.produitNom(), ligne.quantite(), Integer::sum);
            }
        }

        List<ProduitVenduDto> classement = quantitesParProduit.entrySet().stream()
                .map(entry -> new ProduitVenduDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ProduitVenduDto::quantiteVendue).reversed())
                .limit(10)
                .toList();
        topProduits.setAll(classement);
    }
}

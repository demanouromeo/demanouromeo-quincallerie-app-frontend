package com.mvogt.quincaillerie.client.controller;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mvogt.quincaillerie.client.model.LigneVenteResponse;
import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.model.ProduitVenduDto;
import com.mvogt.quincaillerie.client.model.VenteResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
    private LineChart<String, Number> ventesTendanceChart;
    @FXML
    private BarChart<String, Number> topProduitsChart;
    @FXML
    private Label statusLabel;

    private static final DateTimeFormatter FORMAT_JOUR = DateTimeFormatter.ofPattern("dd/MM");
    private static final int NB_JOURS_TENDANCE = 14;

    private final ObservableList<ProduitVenduDto> topProduits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProduitNom.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().produitNom()));
        colQuantiteVendue.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().quantiteVendue()));
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

        Map<LocalDate, BigDecimal> totauxParJour = new LinkedHashMap<>();
        LocalDate debutTendance = aujourdHui.minusDays(NB_JOURS_TENDANCE - 1);
        for (LocalDate jour = debutTendance; !jour.isAfter(aujourdHui); jour = jour.plusDays(1)) {
            totauxParJour.put(jour, BigDecimal.ZERO);
        }

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
            totauxParJour.computeIfPresent(dateVente, (jour, total) -> total.add(vente.montantTotal()));
        }

        ventesJourLabel.setText(nbJour + " vente(s) — " + totalJour + " FCFA");
        ventesSemaineLabel.setText(nbSemaine + " vente(s) — " + totalSemaine + " FCFA");
        ventesMoisLabel.setText(nbMois + " vente(s) — " + totalMois + " FCFA");

        XYChart.Series<String, Number> serieTendance = new XYChart.Series<>();
        totauxParJour.forEach((jour, total) ->
                serieTendance.getData().add(new XYChart.Data<>(jour.format(FORMAT_JOUR), total)));
        ventesTendanceChart.setData(FXCollections.observableArrayList(serieTendance));
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

        XYChart.Series<String, Number> serieTopProduits = new XYChart.Series<>();
        classement.stream()
                .limit(6)
                .forEach(produit -> serieTopProduits.getData()
                        .add(new XYChart.Data<>(produit.produitNom(), produit.quantiteVendue())));
        topProduitsChart.setData(FXCollections.observableArrayList(serieTopProduits));
    }
}

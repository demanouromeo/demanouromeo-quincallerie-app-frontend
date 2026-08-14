package com.mvogt.quincaillerie.client.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.mvogt.quincaillerie.client.model.ProduitResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * Vue d'ensemble du stock avec actualisation automatique periodique. L'appel HTTP (bloquant,
 * {@link com.mvogt.quincaillerie.client.service.ApiClient} est synchrone) tourne sur le thread de
 * l'executeur planifie, jamais sur le thread JavaFX, pour ne pas geler l'interface a chaque tick.
 */
public class DashboardTabController {

    private static final long PERIODE_SECONDES = 30;
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private CheckBox actualisationAutoCheckBox;
    @FXML
    private Label totalProduitsLabel;
    @FXML
    private Label sousSeuilLabel;
    @FXML
    private Label derniereMajLabel;
    @FXML
    private TextField rechercheField;
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
    private TableColumn<ProduitResponse, Integer> colStockActuel;
    @FXML
    private TableColumn<ProduitResponse, Integer> colSeuilAlerte;
    @FXML
    private TableColumn<ProduitResponse, ProduitResponse> colStatut;
    @FXML
    private PieChart statutChart;
    @FXML
    private BarChart<Number, String> categorieChart;
    @FXML
    private Label statusLabel;

    private final ObservableList<ProduitResponse> produits = FXCollections.observableArrayList();
    private final FilteredList<ProduitResponse> produitsFiltres = new FilteredList<>(produits, p -> true);
    private final ScheduledExecutorService executeur = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory());
    private ScheduledFuture<?> tacheAuto;

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
        colStockActuel.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().stockActuel()));
        colSeuilAlerte.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().seuilAlerte()));
        colStatut.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ProduitResponse produit, boolean vide) {
                super.updateItem(produit, vide);
                if (vide || produit == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label();
                if (produit.stockActuel() <= 0) {
                    badge.setText("Rupture");
                    badge.getStyleClass().setAll("badge", "badge-danger");
                } else if (produit.stockActuel() <= produit.seuilAlerte()) {
                    badge.setText("Stock faible");
                    badge.getStyleClass().setAll("badge", "badge-warning");
                } else {
                    badge.setText("En stock");
                    badge.getStyleClass().setAll("badge", "badge-success");
                }
                setGraphic(badge);
            }
        });
        produitsTable.setItems(produitsFiltres);

        rechercheField.textProperty().addListener((obs, ancien, texte) -> {
            String recherche = texte == null ? "" : texte.strip().toLowerCase();
            produitsFiltres.setPredicate(produit -> recherche.isEmpty()
                    || produit.reference().toLowerCase().contains(recherche)
                    || produit.nom().toLowerCase().contains(recherche)
                    || produit.categorieNom().toLowerCase().contains(recherche));
        });

        actualisationAutoCheckBox.selectedProperty().addListener((obs, ancien, actif) -> {
            if (actif) {
                demarrerActualisationAuto();
            } else {
                arreterActualisationAuto();
            }
        });

        actualiser();
        if (actualisationAutoCheckBox.isSelected()) {
            demarrerActualisationAuto();
        }
    }

    @FXML
    private void handleActualiser() {
        actualiser();
    }

    /** Arrete l'executeur planifie ; appele par BackofficeController a la deconnexion. */
    public void detenerActualisation() {
        executeur.shutdownNow();
    }

    private void demarrerActualisationAuto() {
        tacheAuto = executeur.scheduleAtFixedRate(this::actualiser, PERIODE_SECONDES, PERIODE_SECONDES, TimeUnit.SECONDS);
    }

    private void arreterActualisationAuto() {
        if (tacheAuto != null) {
            tacheAuto.cancel(false);
            tacheAuto = null;
        }
    }

    private void actualiser() {
        executeur.submit(() -> {
            try {
                List<ProduitResponse> resultat =
                        Session.getInstance().getApiClient().getList("/produits", ProduitResponse.class);
                Platform.runLater(() -> afficher(resultat));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Erreur chargement stock : " + e.getMessage()));
            }
        });
    }

    private void afficher(List<ProduitResponse> resultat) {
        produits.setAll(resultat);
        totalProduitsLabel.setText(String.valueOf(resultat.size()));
        long sousSeuil = resultat.stream().filter(p -> p.stockActuel() <= p.seuilAlerte()).count();
        sousSeuilLabel.setText(String.valueOf(sousSeuil));
        derniereMajLabel.setText(LocalTime.now().format(FORMAT_HEURE));
        statusLabel.setText("");
        mettreAJourGraphiques(resultat);
    }

    private void mettreAJourGraphiques(List<ProduitResponse> resultat) {
        long rupture = resultat.stream().filter(p -> p.stockActuel() <= 0).count();
        long stockFaible = resultat.stream()
                .filter(p -> p.stockActuel() > 0 && p.stockActuel() <= p.seuilAlerte()).count();
        long enStock = resultat.size() - rupture - stockFaible;
        statutChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("En stock (" + enStock + ")", enStock),
                new PieChart.Data("Stock faible (" + stockFaible + ")", stockFaible),
                new PieChart.Data("Rupture (" + rupture + ")", rupture)));

        Map<String, Integer> stockParCategorie = new LinkedHashMap<>();
        for (ProduitResponse produit : resultat) {
            stockParCategorie.merge(produit.categorieNom(), produit.stockActuel(), Integer::sum);
        }

        // Une serie par categorie (plutot qu'une seule serie multi-points) pour que
        // chaque barre ait sa propre couleur (CSS .seriesN) et sa propre entree de
        // legende — meme pattern que RapportsTabController#calculerTopProduits.
        ObservableList<XYChart.Series<Number, String>> series = FXCollections.observableArrayList();
        stockParCategorie.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .forEach(entry -> {
                    XYChart.Series<Number, String> serie = new XYChart.Series<>();
                    serie.setName(entry.getKey());
                    XYChart.Data<Number, String> point = new XYChart.Data<>(entry.getValue(), entry.getKey());
                    installerTooltipCategorie(point, entry.getKey(), entry.getValue());
                    serie.getData().add(point);
                    series.add(serie);
                });
        categorieChart.setData(series);
    }

    private void installerTooltipCategorie(XYChart.Data<Number, String> point, String categorie, int stock) {
        Tooltip tooltip = new Tooltip(categorie + " : " + stock + " unite(s) en stock");
        point.nodeProperty().addListener((obs, ancienNoeud, nouveauNoeud) -> {
            if (nouveauNoeud != null) {
                Tooltip.install(nouveauNoeud, tooltip);
            }
        });
    }

    private static ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "dashboard-actualisation");
            thread.setDaemon(true);
            return thread;
        };
    }
}

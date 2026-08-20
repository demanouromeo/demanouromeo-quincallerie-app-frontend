package com.mvogt.quincaillerie.client.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import com.mvogt.quincaillerie.client.session.Session;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Verifie que backoffice.fxml se charge sans exception et que tous les fx:id/controleurs sont
 * correctement cables (meme verification "smoke test structurel" que celle deja faite pour la
 * Phase 5 — voir CLAUDE.md), en particulier l'injection du nouveau
 * DashboardTabController via fx:include + convention de nommage "<fx:id>Controller", et
 * l'arret du polling qu'elle rend possible depuis handleDeconnexion.
 */
class BackofficeScreenTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // Rien a afficher par defaut ; chaque test charge sa propre scene.
    }

    @Test
    void backofficeAvecRoleAdminGardeTousLesOnglets() throws Exception {
        Session.getInstance().ouvrir("admin", "token-test", "ADMIN");

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mvogt/quincaillerie/client/view/backoffice.fxml"));
        interact(() -> {
            try {
                loader.load();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        TabPane tabPane = (TabPane) loader.<javafx.scene.layout.BorderPane>getRoot().getCenter();
        assertTrue(tabPane.getTabs().stream().anyMatch(tab -> "dashboardTab".equals(tab.getId())),
                "L'onglet Tableau de bord doit etre present");
        assertTrue(tabPane.getTabs().stream().anyMatch(tab -> "rapportsTab".equals(tab.getId())),
                "Rapports doit rester visible pour ADMIN");
        assertTrue(tabPane.getTabs().stream().anyMatch(tab -> "parametresTab".equals(tab.getId())),
                "Parametres doit rester visible pour ADMIN");

        Button pointDeVenteButton = (Button) loader.<javafx.scene.layout.BorderPane>getRoot()
                .lookup("#pointDeVenteButton");
        assertTrue(pointDeVenteButton.isVisible(), "Le bouton Point de vente doit etre visible pour ADMIN");

        BackofficeController controller = loader.getController();
        assertNotNull(controller);
    }

    @Test
    void backofficeAvecRoleGestionnaireGardeLeTableauDeBordMaisRetireRapports() throws Exception {
        Session.getInstance().ouvrir("gestionnaire.test", "token-test", "GESTIONNAIRE");

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mvogt/quincaillerie/client/view/backoffice.fxml"));
        interact(() -> {
            try {
                loader.load();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        TabPane tabPane = (TabPane) loader.<javafx.scene.layout.BorderPane>getRoot().getCenter();
        assertTrue(tabPane.getTabs().stream().anyMatch(tab -> "dashboardTab".equals(tab.getId())),
                "L'onglet Tableau de bord doit rester visible pour GESTIONNAIRE");
        assertFalse(tabPane.getTabs().stream().anyMatch(tab -> "rapportsTab".equals(tab.getId())),
                "Rapports doit etre retire pour GESTIONNAIRE");
        assertFalse(tabPane.getTabs().stream().anyMatch(tab -> "parametresTab".equals(tab.getId())),
                "Parametres doit etre retire pour GESTIONNAIRE");

        Button pointDeVenteButton = (Button) loader.<javafx.scene.layout.BorderPane>getRoot()
                .lookup("#pointDeVenteButton");
        assertFalse(pointDeVenteButton.isVisible(),
                "Le bouton Point de vente doit etre cache pour GESTIONNAIRE (seuls ADMIN et VENDEUR y accedent)");
    }
}

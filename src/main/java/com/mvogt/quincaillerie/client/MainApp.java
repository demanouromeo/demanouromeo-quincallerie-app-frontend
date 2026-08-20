package com.mvogt.quincaillerie.client;

import java.io.IOException;

import atlantafx.base.theme.PrimerLight;

import com.mvogt.quincaillerie.client.model.ParametresResponse;
import com.mvogt.quincaillerie.client.session.Session;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        primaryStage.setTitle("Quincaillerie Mvogt");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("view/logo.png")));
        // Plancher de redimensionnement : en dessous, les cartes cote a cote de
        // l'ecran de vente (recherche produit + panier) se compriment au point de
        // rendre la liste de resultats illisible (colonnes ecrasees, boutons
        // tronques en "..."). 900x600 est la plus petite taille naturelle des
        // trois ecrans (login.fxml) ; vente.fxml (1000x640) et backoffice.fxml
        // (1200x720) restent donc affiches a leur taille normale au demarrage,
        // seul le redimensionnement manuel vers le bas est borne.
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(new Scene(chargerEcranInitial()));
        primaryStage.show();
    }

    /** Si une session a ete "memorisee" (case "Se souvenir de moi", voir
     *  Session.sauvegarderSession) lors d'un lancement precedent, tente de la reprendre
     *  directement — verifiee par un appel leger a /api/parametres (ouvert a tout role
     *  authentifie) plutot que de repasser par l'ecran de connexion. Equivalent desktop de la
     *  persistance localStorage cote Angular. En cas d'echec (token expire, serveur
     *  injoignable), la session memorisee est effacee et l'ecran de connexion normal
     *  s'affiche. */
    private Parent chargerEcranInitial() throws IOException {
        Session.SessionSauvegardee sauvegardee = Session.getInstance().chargerSessionSauvegardee();
        if (sauvegardee != null) {
            Session.getInstance().ouvrir(sauvegardee.login(), sauvegardee.token(), sauvegardee.role());
            try {
                Session.getInstance().getApiClient().get("/parametres", ParametresResponse.class);
                boolean backoffice = "GESTIONNAIRE".equals(sauvegardee.role()) || "ADMIN".equals(sauvegardee.role());
                return new FXMLLoader(getClass().getResource(backoffice ? "view/backoffice.fxml" : "view/vente.fxml")).load();
            } catch (Exception e) {
                Session.getInstance().reinitialiser();
            }
        }
        return new FXMLLoader(getClass().getResource("view/login.fxml")).load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

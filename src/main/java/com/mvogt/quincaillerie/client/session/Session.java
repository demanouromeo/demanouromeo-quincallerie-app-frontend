package com.mvogt.quincaillerie.client.session;

import java.util.prefs.Preferences;

import com.mvogt.quincaillerie.client.config.AppConfig;
import com.mvogt.quincaillerie.client.service.ApiClient;

/** Etat de la session utilisateur courante (poste vendeur mono-utilisateur). */
public final class Session {

    private static final Session INSTANCE = new Session();
    private static final String PREF_LOGIN = "login";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_ROLE = "role";

    private final ApiClient apiClient = new ApiClient(AppConfig.getApiBaseUrl());
    private final Preferences preferences = Preferences.userNodeForPackage(Session.class);
    private String login;
    private String role;

    private Session() {
    }

    public static Session getInstance() {
        return INSTANCE;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void ouvrir(String login, String token, String role) {
        this.login = login;
        this.role = role;
        apiClient.setAuthToken(token);
    }

    public void reinitialiser() {
        this.login = null;
        this.role = null;
        apiClient.setAuthToken(null);
        effacerSessionSauvegardee();
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    /** Persiste la session (equivalent desktop du "Se souvenir de moi" cote Angular, qui
     *  utilise localStorage) pour la retrouver au prochain lancement de l'application, sans
     *  redemander les identifiants — voir MainApp.chargerEcranInitial(). */
    public void sauvegarderSession(String login, String token, String role) {
        preferences.put(PREF_LOGIN, login);
        preferences.put(PREF_TOKEN, token);
        preferences.put(PREF_ROLE, role);
    }

    public void effacerSessionSauvegardee() {
        preferences.remove(PREF_LOGIN);
        preferences.remove(PREF_TOKEN);
        preferences.remove(PREF_ROLE);
    }

    public SessionSauvegardee chargerSessionSauvegardee() {
        String loginSauvegarde = preferences.get(PREF_LOGIN, null);
        String tokenSauvegarde = preferences.get(PREF_TOKEN, null);
        String roleSauvegarde = preferences.get(PREF_ROLE, null);
        if (loginSauvegarde == null || tokenSauvegarde == null || roleSauvegarde == null) {
            return null;
        }
        return new SessionSauvegardee(loginSauvegarde, tokenSauvegarde, roleSauvegarde);
    }

    public record SessionSauvegardee(String login, String token, String role) {
    }
}

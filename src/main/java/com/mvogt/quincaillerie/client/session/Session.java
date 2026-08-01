package com.mvogt.quincaillerie.client.session;

import com.mvogt.quincaillerie.client.config.AppConfig;
import com.mvogt.quincaillerie.client.service.ApiClient;

/** Etat de la session utilisateur courante (poste vendeur mono-utilisateur). */
public final class Session {

    private static final Session INSTANCE = new Session();

    private final ApiClient apiClient = new ApiClient(AppConfig.getApiBaseUrl());
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
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
}

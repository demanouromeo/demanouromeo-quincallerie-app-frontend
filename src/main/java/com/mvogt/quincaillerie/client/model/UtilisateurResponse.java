package com.mvogt.quincaillerie.client.model;

public record UtilisateurResponse(Long id, String nom, String login, String role, boolean actif) {
}

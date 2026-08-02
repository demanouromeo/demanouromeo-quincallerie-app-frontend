package com.mvogt.quincaillerie.client.model;

public record UtilisateurResponse(Long id, String nom, String login, String email, String role, boolean actif) {
}

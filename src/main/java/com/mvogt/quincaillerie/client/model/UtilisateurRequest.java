package com.mvogt.quincaillerie.client.model;

public record UtilisateurRequest(String nom, String login, String email, String motDePasse, String role, boolean actif) {
}

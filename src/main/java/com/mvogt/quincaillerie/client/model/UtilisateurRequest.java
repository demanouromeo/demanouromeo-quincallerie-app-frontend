package com.mvogt.quincaillerie.client.model;

public record UtilisateurRequest(String nom, String login, String motDePasse, String role, boolean actif) {
}

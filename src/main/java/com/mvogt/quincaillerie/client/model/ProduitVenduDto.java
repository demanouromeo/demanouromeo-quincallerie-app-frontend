package com.mvogt.quincaillerie.client.model;

/** Ligne agregee pour le rapport "produits les plus vendus", calculee cote client. */
public record ProduitVenduDto(String produitNom, int quantiteVendue) {
}

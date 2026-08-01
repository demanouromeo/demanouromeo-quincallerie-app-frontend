package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

/** Ligne du panier de vente en cours de saisie, cote client (pas encore envoyee au backend). */
public class LignePanier {

    private final ProduitResponse produit;
    private int quantite;

    public LignePanier(ProduitResponse produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public ProduitResponse getProduit() {
        return produit;
    }

    public String getProduitNom() {
        return produit.nom();
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return produit.prixVente();
    }

    public BigDecimal getSousTotal() {
        return produit.prixVente().multiply(BigDecimal.valueOf(quantite));
    }
}

package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

/** Ligne d'approvisionnement en cours de saisie, cote client (pas encore envoyee au backend). */
public class LigneApproSaisie {

    private final ProduitResponse produit;
    private final int quantite;
    private final BigDecimal prixAchatUnitaire;

    public LigneApproSaisie(ProduitResponse produit, int quantite, BigDecimal prixAchatUnitaire) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixAchatUnitaire = prixAchatUnitaire;
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

    public BigDecimal getPrixAchatUnitaire() {
        return prixAchatUnitaire;
    }

    public BigDecimal getSousTotal() {
        return prixAchatUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}

package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

public record LigneVenteResponse(
        Long produitId,
        String produitNom,
        int quantite,
        BigDecimal prixVenteUnitaire,
        BigDecimal sousTotal
) {
}

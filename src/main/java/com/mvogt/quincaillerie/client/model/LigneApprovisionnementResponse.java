package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

public record LigneApprovisionnementResponse(
        Long produitId,
        String produitNom,
        int quantite,
        BigDecimal prixAchatUnitaire,
        BigDecimal sousTotal
) {
}

package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

public record ProduitResponse(
        Long id,
        String reference,
        String nom,
        Long categorieId,
        String categorieNom,
        String unite,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        int seuilAlerte,
        int stockActuel
) {
}

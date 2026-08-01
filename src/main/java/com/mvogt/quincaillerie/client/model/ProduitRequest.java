package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

public record ProduitRequest(
        String reference,
        String nom,
        Long categorieId,
        String unite,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        int seuilAlerte,
        int stockActuel
) {
}

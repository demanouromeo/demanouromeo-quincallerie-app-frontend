package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApprovisionnementResponse(
        Long id,
        Instant dateAppro,
        Long fournisseurId,
        String fournisseurNom,
        String gestionnaireLogin,
        BigDecimal montantTotal,
        List<LigneApprovisionnementResponse> lignes
) {
}

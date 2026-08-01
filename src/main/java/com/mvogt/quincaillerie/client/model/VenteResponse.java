package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VenteResponse(
        Long id,
        Instant dateVente,
        String vendeurLogin,
        BigDecimal montantTotal,
        List<LigneVenteResponse> lignes
) {
}

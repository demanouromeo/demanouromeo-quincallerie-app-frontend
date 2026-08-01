package com.mvogt.quincaillerie.client.model;

import java.math.BigDecimal;

public record LigneApprovisionnementRequest(Long produitId, int quantite, BigDecimal prixAchatUnitaire) {
}

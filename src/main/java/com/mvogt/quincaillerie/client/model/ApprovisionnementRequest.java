package com.mvogt.quincaillerie.client.model;

import java.util.List;

public record ApprovisionnementRequest(Long fournisseurId, List<LigneApprovisionnementRequest> lignes) {
}

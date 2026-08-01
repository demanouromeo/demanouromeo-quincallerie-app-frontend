package com.mvogt.quincaillerie.client.model;

import java.util.List;

public record VenteRequest(List<LigneVenteRequest> lignes) {
}

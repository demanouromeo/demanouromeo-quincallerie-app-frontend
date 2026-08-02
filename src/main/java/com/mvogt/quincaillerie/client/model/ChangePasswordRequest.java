package com.mvogt.quincaillerie.client.model;

public record ChangePasswordRequest(String ancienMotDePasse, String nouveauMotDePasse) {
}

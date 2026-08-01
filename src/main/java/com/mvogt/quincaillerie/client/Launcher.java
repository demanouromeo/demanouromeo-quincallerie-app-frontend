package com.mvogt.quincaillerie.client;

/**
 * Point d'entree distinct de {@link MainApp} pour le lancement natif jpackage : le JVM refuse de
 * demarrer directement une classe qui etend {@code javafx.application.Application} depuis un
 * classpath non modulaire (pas de module-info.java ici) avec "JavaFX runtime components are
 * missing" — une classe main separee qui ne l'etend pas contourne cette verification.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}

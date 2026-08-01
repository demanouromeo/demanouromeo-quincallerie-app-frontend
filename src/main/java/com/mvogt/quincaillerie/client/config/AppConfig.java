package com.mvogt.quincaillerie.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Configuration du client : URL de base de l'API, chargee depuis client.properties (classpath). */
public final class AppConfig {

    private static final String DEFAULT_API_BASE_URL = "http://localhost:8082/api";
    private static final Properties PROPERTIES = load();

    private AppConfig() {
    }

    public static String getApiBaseUrl() {
        return PROPERTIES.getProperty("api.baseUrl", DEFAULT_API_BASE_URL);
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/client.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            // Fichier de config optionnel : les valeurs par defaut s'appliquent.
        }
        return properties;
    }
}

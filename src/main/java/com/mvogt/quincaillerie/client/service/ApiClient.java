package com.mvogt.quincaillerie.client.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Client HTTP generique vers l'API REST du backend Spring Boot. */
public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String authToken;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(path).GET().build();
        return objectMapper.readValue(sendRaw(request), responseType);
    }

    public <T> List<T> getList(String path, Class<T> elementType) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(path).GET().build();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return objectMapper.readValue(sendRaw(request), listType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        return objectMapper.readValue(sendRaw(request), responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
                .build();
        return objectMapper.readValue(sendRaw(request), responseType);
    }

    public void delete(String path) throws IOException, InterruptedException {
        HttpRequest request = requestBuilder(path).DELETE().build();
        sendRaw(request);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + path));
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    private String sendRaw(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Erreur API (" + response.statusCode() + "): " + response.body());
        }
        return response.body();
    }
}

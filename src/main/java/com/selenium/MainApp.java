package com.selenium;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Simple entry point that performs a quick health ping to https://chamilo.org/en/.
 */
public final class MainApp {

    private static final String TARGET_URL = "https://chamilo.org/en/";

    private MainApp() {
        // utility class
    }

    public static void main(String[] args) {
        System.out.println("Memulai pengecekan ringan ke " + TARGET_URL);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TARGET_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            System.out.printf("Status HTTP: %d%n", response.statusCode());
        } catch (IOException | InterruptedException ex) {
            System.err.println("Gagal menghubungi situs: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}

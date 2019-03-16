package org.trading.health.netty;

import com.codahale.metrics.health.HealthCheck;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.health.HealthCheckServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckServerTest {

    private HealthCheckServer server;
    private HttpClient client;
    private int port;

    @BeforeEach
    void start() {
        port = getAvailablePort();
        server = new HealthCheckServer("localhost", port, "version", "name");
        server.start();
        client = newHttpClient();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void should_respond_with_svg_content() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port + "/badge";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("image/svg+xml");
        assertThat(response.body()).contains("ok");
    }

    @Test
    void should_respond_with_bootstrap_css() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port + "/static/bootstrap.min.css";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("text/css");
        assertThat(response.body()).contains("bootstrap.min.css");
    }

    @Test
    void should_respond_with_bootstrap_js() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port + "/static/bootstrap.min.js";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("application/javascript");
        assertThat(response.body()).contains("bootstrap.min.js");
    }

    @Test
    void should_respond_with_jquery_js() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port + "/static/jquery-3.3.1.min.js";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("application/javascript");
        assertThat(response.body()).contains("jQuery");
    }

    @Test
    public void should_stop_a_not_started_server() {
        HealthCheckServer server = new HealthCheckServer("localhost", port, "version", "name");
        server.stop();
    }


    @Test
    void should_respond_with_favicon() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port + "/favicon.ico";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("image/x-icon");
        assertThat(response.body()).contains("PNG");
    }

    @Test
    void should_respond_with_healthy_check_status() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port;
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();
        server.register("success", new HealthCheck() {
            @Override
            protected Result check() {
                return healthy();
            }
        });

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("text/html");
        assertThat(response.body()).contains("Overall Status");
    }

    @Test
    void should_respond_with_unhealthy_check_status() throws IOException, InterruptedException {

        // Given
        String uri = "http://localhost" + ":" + port;
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();
        server.register("success", new HealthCheck() {
            @Override
            protected Result check() {
                return unhealthy("heathcheck is unhealthy");
            }
        });

        // When
        HttpResponse<String> response = client.send(request, ofString());

        // Then
        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.headers().map().get(CONTENT_TYPE)).containsExactly("text/html");
        assertThat(response.body()).contains("heathcheck is unhealthy");
    }


    static int getAvailablePort() {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Objects.requireNonNull(s).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
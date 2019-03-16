package org.trading.health;

import com.codahale.metrics.health.HealthCheck;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.time.temporal.ChronoUnit.SECONDS;

public class HealthCheckClient extends HealthCheck {

    private final HttpClient client;

    private final String host;
    private final int port;

    public HealthCheckClient(String host, int port) {
        this.host = host;
        this.port = port;
        client = newHttpClient();
    }

    @Override
    protected Result check() throws Exception {
        String uri = "http://" + host + ":" + port + "/v1/healthcheck";
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.of(1, SECONDS))
                .uri(URI.create(uri))
                .build();
        HttpResponse<String> response = client.send(request, ofString());
        if (response.statusCode() == 200) {
            return Result.healthy(uri);
        }
        return Result.unhealthy(uri);

    }
}

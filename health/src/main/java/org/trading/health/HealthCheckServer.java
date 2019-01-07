package org.trading.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;

public final class HealthCheckServer {
    private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final HttpServer httpServer;

    public HealthCheckServer(String host, int port, String appVersion, String appName) {
        try {
            HealthCheckReporter healthCheckReport = new HealthCheckReporter(appVersion, appName);
            httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            httpServer.createContext("/", exchange -> {
                SortedMap<String, HealthCheck.Result> checks = healthCheckRegistry.runHealthChecks();
                boolean isHealthy = checks.values().stream().allMatch(HealthCheck.Result::isHealthy);
                byte[] bytes = healthCheckReport.apply(checks).getBytes();
                int statusCode = isHealthy ? HTTP_OK : HttpURLConnection.HTTP_INTERNAL_ERROR;
                exchange.sendResponseHeaders(statusCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            BadgeReporter badgeReporter = new BadgeReporter(appVersion);
            httpServer.createContext("/badge", exchange -> {
                SortedMap<String, HealthCheck.Result> checks = healthCheckRegistry.runHealthChecks();
                byte[] bytes = badgeReporter.apply(checks).getBytes();
                exchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                exchange.sendResponseHeaders(HTTP_OK, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            URI uri = Resources.getResource("static").toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = newFileSystem(uri, emptyMap());
                myPath = fileSystem.getPath("static");
            } else {
                myPath = Paths.get(uri);
            }

            try (Stream<Path> paths = Files.walk(myPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> httpServer.createContext("/static/" + path.getFileName(), exchange -> {
                            Path p = Paths.get(uri).relativize(path);
                            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("static/" + p);
                            byte[] bytes = ByteStreams.toByteArray(inputStream);
                            try (OutputStream os = exchange.getResponseBody()) {
                                exchange.sendResponseHeaders(HTTP_OK, bytes.length);
                                os.write(bytes);
                            }
                        }));
            }

            healthCheckRegistry.register("Thread Deadlock", new ThreadDeadlockHealthCheck());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Cannot bind HealthCheck server", e);
        }
    }

    public static void register(String name, HealthCheck healthCheck) {
        healthCheckRegistry.register(name, healthCheck);
    }

    public void start() {
        started.set(true);
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}

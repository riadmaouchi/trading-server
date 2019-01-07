package org.trading.health;

import com.codahale.metrics.health.HealthCheck;

import java.util.Map;
import java.util.function.Function;

public final class BadgeReporter implements Function<Map<String, HealthCheck.Result>, String> {
    private final String appVersion;

    public BadgeReporter(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String apply(Map<String, HealthCheck.Result> checks) {
        boolean isHealthy = checks.values().stream().allMatch(HealthCheck.Result::isHealthy);
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"94\" height=\"20\">" +
                "<linearGradient id=\"b\" x2=\"0\" y2=\"100%\">" +
                "<stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/>" +
                "<stop offset=\"1\" stop-opacity=\".1\"/>" +
                "</linearGradient>" +
                "<clipPath id=\"a\"><rect width=\"94\" height=\"20\" rx=\"3\" fill=\"#fff\"/></clipPath>" +
                "<g clip-path=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h67v20H0z\"/>" +
                "<path fill=\"" +
                (isHealthy ? "#4c1" : "#e05d44") +
                "\" d=\"M67 0h27v20H67z\"/>" +
                "<path fill=\"url(#b)\" d=\"M0 0h94v20H0z\"/></g>" +
                "<g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans, Verdana, Geneva, sans - serif\" font-size=\"110\">" +
                "<text x=\"345\" y=\"150\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"570\">" +
                appVersion +
                "</text>" +
                "<text x=\"345\" y=\"140\" transform=\"scale(.1)\" textLength=\"570\">" +
                appVersion +
                "</text>" +
                "<text x=\"795\" y=\"150\" fill=\"#010101\" fill-opacity=\".3\" transform=\"scale(.1)\" textLength=\"170\">" +
                (isHealthy ? "ok" : "ko") +
                "</text>" +
                "<text x=\"795\" y=\"140\" transform=\"scale(.1)\" textLength=\"170\">" +
                (isHealthy ? "ok" : "ko") +
                "</text>" +
                "</g></svg>";
    }
}

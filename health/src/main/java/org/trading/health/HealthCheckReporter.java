package org.trading.health;

import com.codahale.metrics.health.HealthCheck.Result;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class HealthCheckReporter implements Function<Map<String, Result>, String> {
    private final String appVersion;
    private final String appName;

    public HealthCheckReporter(String appVersion,
                               String appName) {
        this.appVersion = appVersion;
        this.appName = appName;
    }

    public String apply(Map<String, Result> checks) {
        boolean isHealthy = checks.values().stream().allMatch(Result::isHealthy);

        StringBuilder report = new StringBuilder();
        report.append("<!DOCTYPE html>");
        report.append("<html lang=\"en\">");
        report.append("<head>");
        report.append("<meta charset=\"utf-8\">");
        report.append("<title>");
        report.append(appName);
        report.append("</title>");
        report.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
        report.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../static/bootstrap.min.css\">");
        report.append("<style>html,\n" +
                "body {\n" +
                "  height: 100%;\n" +
                "  background-color: #333;\n" +
                "} " +
                "body {\n" +
                "  display: -ms-flexbox;\n" +
                "  display: -webkit-box;\n" +
                "  display: flex;\n" +
                "  -ms-flex-pack: center;\n" +
                "  -webkit-box-pack: center;\n" +
                "  justify-content: center;\n" +
                "  color: #fff;\n" +
                "  text-shadow: 0 .05rem .1rem rgba(0, 0, 0, .5);\n" +
                "  box-shadow: inset 0 0 5rem rgba(0, 0, 0, .5);\n" +
                "}</style>");
        report.append("</head>");
        report.append("<body>");
        report.append("<div class=\"container\">");
        report.append("<h1>");
        report.append(appName);
        report.append(" ");
        report.append(appVersion);
        report.append("</h1>");
        report.append("<div class=\"alert alert-dark\" role=\"alert\"> Overall Status ");
        report.append(isHealthy ? "<span class=\"badge badge-pill badge-success\">Healthy</span>" : "<span class=\"badge badge-pill badge-danger\">UnHealthy</span>");
        report.append("</div>");
        report.append("<table class=\"table table-striped table-dark\"");

        report.append("<tbody>");

        checks.forEach((name, result) -> {
            report.append("<tr>");
            report.append("<td>");

            report.append(name);
            report.append("</td>");
            report.append("<td>");
            report.append("<small>");
            Optional.ofNullable(result.getMessage()).ifPresent(message -> report.append(result.getMessage()));
            report.append("</small></td>");

            report.append("<td>");
            report.append(result.isHealthy() ?
                    "<span class=\"badge badge-pill badge-success\">Healthy</span>"
                    :
                    "<span class=\"badge badge-pill badge-danger\">UnHealthy</span>");
            report.append("</td>");
            report.append("</tr>");
        });
        report.append("</tbody>");
        report.append("</table>");
        report.append("</div>");
        report.append("<script src=\"../static/jquery-3.3.1.min.js\"></script>");
        report.append("<script src=\"../static/bootstrap.min.js\"></script>");
        report.append("</body>");
        report.append("</html>");
        return report.toString();
    }
}

package org.trading.pricing;

import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.Service;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckServer;
import org.trading.pricing.web.PricingServer;
import org.trading.pricing.web.PricingServer.PricingServerConfiguration;

import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.pricing.ServiceName.PRICING;
import static org.trading.pricing.ServiceName.SERVER;

public final class PricingWebMain {

    public static void main(String[] args) throws Exception {
        new PricingWebMain().start();
    }

    private void start() throws Exception {

        String host = ofNullable(getenv("HOSTNAME")).orElse("localhost");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        String consulUrl = getenv("CONSUL.URL");
        RemoteProviderFactory.RemoteProvider provider = ofNullable(consulUrl)
                .map(s -> CONSUL).orElse(DEFAULT);

        int httpMonitoringPort = parseInt(ofNullable(getenv("HTTP.MONITORING.PORT")).orElse("9999"));

        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();
        ServiceRegistry serviceRegistry = getFactory(provider, healthCheckServer, consulUrl).getServiceConfiguration();

        Stream.of(ServiceName.values()).map(service -> new Service(
                "pricer",
                parseInt(ofNullable(System.getenv(service.name() + "." + service.protocol + ".PORT")).orElse(String.valueOf(service.defaultPort))),
                3L,
                service.name(),
                httpMonitoringPort,
                service.protocol.toLowerCase()
        )).forEach(service -> serviceRegistry.register(service, host, "localhost"));



        new PricingServer(serviceRegistry, host, new PricingServerConfiguration() {

            @Override
            public int getHttpPort() {
                return parseInt(ofNullable(System.getenv(SERVER.name() + "." + SERVER.protocol + ".PORT"))
                        .orElse(String.valueOf(SERVER.defaultPort)));
            }

            @Override
            public int getPricingPort() {
                return parseInt(ofNullable(System.getenv(PRICING.name() + "." + PRICING.protocol + ".PORT"))
                        .orElse(String.valueOf(PRICING.defaultPort)));
            }
        }, healthCheckServer).start();
    }
}

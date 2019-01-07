package org.trading.pricing;

import org.trading.discovery.Service;
import org.trading.discovery.RemoteProviderFactory.RemoteProvider;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HealthCheckServer;
import org.trading.configuration.Configuration;
import org.trading.pricing.web.PricingServer;
import org.trading.pricing.web.PricingServer.PricingServerConfiguration;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.configuration.Configuration.create;

public final class PricingWebMain {

    public static void main(String[] args) throws Exception {
        new PricingWebMain().start();
    }

    private void start() throws Exception {

        String host = getProperty("docker.container.id", "localhost");
        String consulEnabled = getProperty("consul.enabled", "false");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        Configuration configuration = create();
        RemoteProvider provider = parseBoolean(consulEnabled) ? CONSUL : DEFAULT;
        ServiceConfiguration serviceConfiguration = getFactory(provider).getServiceConfiguration();

        int httpMonitoringPort = configuration.getInt("monitoring.port");

        Stream.of(ServiceName.values()).map(serviceName -> new Service(
                "pricer",
                configuration.getInt("service", serviceName.name + ".port"),
                3L,
                serviceName.name,
                httpMonitoringPort,
                serviceName.protocol
        )).forEach(service -> serviceConfiguration.register(service, host));

        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();

        new PricingServer(serviceConfiguration, host, new PricingServerConfiguration() {

            @Override
            public int getHttpPort() {
                return configuration.getInt("service.server.port");
            }

            @Override
            public int getPricingPort() {
                return configuration.getInt("service.pricing.port");
            }
        }).start();
    }
}

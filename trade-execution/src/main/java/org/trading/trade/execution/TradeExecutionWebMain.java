package org.trading.trade.execution;

import org.trading.configuration.Configuration;
import org.trading.discovery.RemoteProviderFactory.RemoteProvider;
import org.trading.discovery.Service;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HealthCheckServer;
import org.trading.trade.execution.web.TradeExecutionServer;
import org.trading.trade.execution.web.TradeExecutionServer.ServerConfiguration;

import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;
import static org.trading.configuration.Configuration.create;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.trade.execution.ServiceName.values;

public final class TradeExecutionWebMain {

    private TradeExecutionWebMain() {
    }

    public static void main(String[] args) throws Exception {
        new TradeExecutionWebMain().start();
    }

    private void start() throws Exception {

        Configuration configuration = create();

        String host = System.getProperty("docker.container.id", "localhost");
        String consulEnabled = System.getProperty("consul.enabled", "false");
        String serviceUrl = getProperty("service.url", "localhost");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        RemoteProvider provider = parseBoolean(consulEnabled) ? CONSUL : DEFAULT;
        ServiceConfiguration serviceConfiguration = getFactory(provider).getServiceConfiguration();

        int httpMonitoringPort = configuration.getInt("monitoring.port");

        Stream.of(values()).map(serviceName -> new Service(
                "order",
                configuration.getInt("service", serviceName.name + ".port"),
                3L,
                serviceName.name,
                httpMonitoringPort,
                serviceName.protocol
        )).forEach(service -> serviceConfiguration.register(service, host, serviceUrl));

        new HealthCheckServer(host, httpMonitoringPort, version, name).start();

        ServerConfiguration serverConfiguration = getServerConfiguration(configuration);
        new TradeExecutionServer(serverConfiguration, serviceConfiguration, host).start();

    }

    private ServerConfiguration getServerConfiguration(Configuration configuration) {
        return new ServerConfiguration() {

            @Override
            public int tradeServerPort() {
                return configuration.getInt("service.tradeserver.port");
            }

            @Override
            public int executionPort() {
                return configuration.getInt("service.execution.port");
            }

            @Override
            public int orderBookPort() {
                return configuration.getInt("service.orderbook.port");
            }

            @Override
            public int blotterPort() {
                return configuration.getInt("service.blotter.port");
            }
        };
    }
}
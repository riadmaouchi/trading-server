package org.trading.trade.execution;

import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.Service;
import org.trading.health.HealthCheckServer;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.trade.execution.web.TradeExecutionServer;
import org.trading.trade.execution.web.TradeExecutionServer.ServerConfiguration;

import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.trade.execution.ServiceName.BLOTTER;
import static org.trading.trade.execution.ServiceName.EXECUTION;
import static org.trading.trade.execution.ServiceName.ORDERBOOK;
import static org.trading.trade.execution.ServiceName.TRADESERVER;
import static org.trading.trade.execution.ServiceName.values;

public final class TradeExecutionWebMain {

    private TradeExecutionWebMain() {
    }

    public static void main(String[] args) throws Exception {
        new TradeExecutionWebMain().start();
    }

    private void start() throws Exception {

        String host = ofNullable(getenv("HOSTNAME")).orElse("localhost");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        int httpMonitoringPort = parseInt(ofNullable(getenv("HTTP.MONITORING.PORT")).orElse("9998"));
        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();

        String consulUrl = getenv("CONSUL.URL");
        RemoteProviderFactory.RemoteProvider provider = ofNullable(consulUrl)
                .map(s -> CONSUL).orElse(DEFAULT);
        ServiceRegistry serviceRegistry = getFactory(provider, healthCheckServer, consulUrl).getServiceConfiguration();



        Stream.of(values()).map(service -> new Service(
                "order",
                parseInt(ofNullable(getenv(service.name() + "." + service.protocol + ".PORT")).orElse(String.valueOf(service.defaultPort))),

                3L,
                service.name(),
                httpMonitoringPort,
                service.protocol.toLowerCase()
        )).forEach(service -> serviceRegistry.register(service, host, "localhost"));



        ServerConfiguration serverConfiguration = getServerConfiguration();
        new TradeExecutionServer(serverConfiguration, serviceRegistry, host, healthCheckServer).start();

    }

    private ServerConfiguration getServerConfiguration() {
        return new ServerConfiguration() {

            @Override
            public int tradeServerPort() {
                return parseInt(ofNullable(System.getenv(TRADESERVER.name() + "." + TRADESERVER.protocol + ".PORT"))
                        .orElse(String.valueOf(TRADESERVER.defaultPort)));
            }

            @Override
            public int executionPort() {
                return parseInt(ofNullable(System.getenv(EXECUTION.name() + "." + EXECUTION.protocol + ".PORT"))
                        .orElse(String.valueOf(EXECUTION.defaultPort)));
            }

            @Override
            public int orderBookPort() {
                return parseInt(ofNullable(System.getenv(ORDERBOOK.name() + "." + ORDERBOOK.protocol + ".PORT"))
                        .orElse(String.valueOf(ORDERBOOK.defaultPort)));
            }

            @Override
            public int blotterPort() {
                return parseInt(ofNullable(System.getenv(BLOTTER.name() + "." + BLOTTER.protocol + ".PORT"))
                        .orElse(String.valueOf(BLOTTER.defaultPort)));
            }
        };
    }
}
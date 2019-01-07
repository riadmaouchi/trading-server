package org.trading.matching.engine;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import org.trading.api.command.SubmitOrder;
import org.trading.discovery.Service;
import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HealthCheckServer;
import org.trading.matching.engine.domain.MatchingEngine;
import org.trading.configuration.Configuration;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.lmax.disruptor.dsl.ProducerType.MULTI;
import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.time.Clock.systemUTC;
import static java.util.Optional.ofNullable;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.configuration.Configuration.create;

public final class MatchingEngineMain {

    public static void main(String[] args) throws IOException {
        new MatchingEngineMain().start();
    }

    private void start() throws IOException {

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        new HTTPServer(1234);

        Counter requests = Counter.build()
                .name("java_app_requests_total")
                .help("Total requests.")
                .register();

        String host = getProperty("docker.container.id", "localhost");
        String consulEnabled = getProperty("consul.enabled", "false");

        Configuration configuration = create();
        RemoteProviderFactory.RemoteProvider provider = parseBoolean(consulEnabled) ? CONSUL : DEFAULT;
        ServiceConfiguration serviceConfiguration = getFactory(provider).getServiceConfiguration();

        Disruptor<Message> outboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                INSTANCE,
                SINGLE,
                new BlockingWaitStrategy()
        );
        outboundDisruptor.start();

        int httpMonitoringPort = configuration.getInt("monitoring.port");

        serviceConfiguration.register(new Service(
                "matchingengine",
                configuration.getInt("service.matchingengine.port"),
                3L,
                "matchingengine",
                httpMonitoringPort,
                "tcp"
        ), host);

        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();

        final OrderEventPublisher eventPublisher = new OrderEventPublisher(outboundDisruptor);

        Map<String, MatchingEngine> engines = new HashMap<>();

        Disruptor<Message> inboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                INSTANCE,
                MULTI,
                new BlockingWaitStrategy()
        );
        inboundDisruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            // requests.inc();
            final SubmitOrder submitOrder = (SubmitOrder) event.event;
            engines.computeIfAbsent(submitOrder.symbol, symbol -> new MatchingEngine(
                    eventPublisher,
                    systemUTC()
            )).submitOrder(submitOrder);
        });
        inboundDisruptor.start();

        serviceConfiguration.discover(outboundDisruptor, "order", "pricer");

        new TcpDataSource(host, configuration.getInt("service.matchingengine.port"), inboundDisruptor, "matchingengine")
                .connect();

    }
}

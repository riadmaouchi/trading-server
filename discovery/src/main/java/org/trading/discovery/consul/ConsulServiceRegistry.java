package org.trading.discovery.consul;

import com.lmax.disruptor.dsl.Disruptor;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.trading.MessageProvider;
import org.trading.discovery.Node;
import org.trading.discovery.Nodes;
import org.trading.discovery.Service;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.discovery.TcpNodes;
import org.trading.health.HealthCheckServer;
import org.trading.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConsulServiceRegistry implements ServiceRegistry {
    private static final Logger LOGGER = getLogger(ConsulServiceRegistry.class);
    private final Map<String, Nodes> nodes = new ConcurrentHashMap<>();
    private final Map<String, TcpNodes> tcpHealthyNodes = new ConcurrentHashMap<>();
    private final ConsulService consulService;
    private final HealthCheckServer healthCheckServer;

    public ConsulServiceRegistry(ConsulService consulService, HealthCheckServer healthCheckServer) {
        this.consulService = consulService;
        this.healthCheckServer = healthCheckServer;
    }

    @Override
    public void register(Service service, String host, String serviceUrl) {
        consulService.register(service, host, serviceUrl);
    }

    @Override
    public void discover(Disruptor<Message> disruptor, Supplier<List<MessageProvider.Message>> supplier, String... services) {
        Stream.of(services).forEach(service -> {
            nodes.put(service, new Nodes());
            tcpHealthyNodes.put(service, new TcpNodes(disruptor, supplier, healthCheckServer));

            consulService.registerHealthyNodeListener(service, newValues -> {
                var healthyTcpNodes = newValues.values().stream()
                        .filter(serviceHealth -> serviceHealth.getService().getTags().contains("tcp"))
                        .map(ServiceHealth::getService)
                        .map(service1 -> new Node(
                                service1.getId(),
                                service1.getAddress(),
                                service1.getPort()
                        )).collect(toList());

                LOGGER.info("{} : {}", service, this.tcpHealthyNodes);
                this.tcpHealthyNodes.get(service).setHealthyNodes(healthyTcpNodes);

                var healthyNodes = newValues.entrySet().stream()
                        .filter(node -> node.getValue().getService().getTags().contains("http"))
                        .map(node -> "http://" + node.getKey().getHost() + ":" + node.getKey().getPort())
                        .collect(toList());

                LOGGER.info("{} : {}", service, healthyNodes);
                nodes.get(service).setHealthyNodes(healthyNodes);
            });
        });
    }

    @Override
    public void update(Consumer<Map<String, Optional<String>>> consumer) {
        consulService.store(consumer);
    }

    @Override
    public void discover(String... services) {
        Stream.of(services).forEach(service -> {
            nodes.put(service, new Nodes());
            consulService.registerHealthyNodeListener(service, newValues -> {
                List<String> healthyNodes = newValues.values().stream()
                        .filter(serviceHealth -> serviceHealth.getService().getTags().contains("http"))
                        .map(serviceHealth -> "http://" + serviceHealth.getService().getAddress() + ":" + serviceHealth.getService().getPort())
                        .collect(toList());

                LOGGER.info("{} : {}", service, healthyNodes);
                nodes.get(service).setHealthyNodes(healthyNodes);
            });
        });
    }

    @Override
    public Optional<String> get(String service) {
        return nodes.get(service).getNodes().stream().findFirst();
    }
}

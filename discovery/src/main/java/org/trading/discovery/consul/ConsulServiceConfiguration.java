package org.trading.discovery.consul;

import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.trading.discovery.*;
import org.trading.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConsulServiceConfiguration implements ServiceConfiguration {
    private static final Logger LOGGER = getLogger(ConsulServiceConfiguration.class);
    private final Map<String, Nodes> nodes = new ConcurrentHashMap<>();
    private final Map<String, TcpNodes> tcpHealthyNodes = new ConcurrentHashMap<>();
    private final ConsulService consulService;

    public ConsulServiceConfiguration(ConsulService consulService) {
        this.consulService = consulService;
    }

    @Override
    public void register(Service service, String host) {
        consulService.register(service, host);
    }

    @Override
    public void discover(Disruptor<Message> disruptor, String... services) {
        Stream.of(services).forEach(service -> {
            nodes.put(service, new Nodes());
            tcpHealthyNodes.put(service, new TcpNodes(disruptor));

            consulService.registerHealthyNodeListener(service, newValues -> {
                List<Node> healthyTcpNodes = newValues.entrySet().stream()
                        .filter(node -> node.getValue().getService().getTags().contains("tcp"))
                        .map(node -> node.getValue().getService())
                        .map(service1 -> new Node(
                                service1.getId(),
                                service1.getAddress(),
                                service1.getPort()
                        )).collect(toList());

                LOGGER.info("{} : {}", service, this.tcpHealthyNodes);
                this.tcpHealthyNodes.get(service).setHealthyNodes(healthyTcpNodes);


                List<String> healthyNodes = newValues.entrySet().stream()
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
                List<String> healthyNodes = newValues.entrySet().stream()
                        .filter(node -> node.getValue().getService().getTags().contains("http"))
                        .map(node -> "http://" + node.getValue().getService().getAddress() + ":" + node.getValue().getService().getPort())
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

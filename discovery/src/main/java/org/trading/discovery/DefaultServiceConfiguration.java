package org.trading.discovery;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.lmax.disruptor.dsl.Disruptor;
import org.trading.messaging.Message;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

public class DefaultServiceConfiguration implements ServiceConfiguration {

    private static final Multimap<String, Map<String, Integer>> services = ArrayListMultimap.create();
    private static final Map<String, String> httpServices = new HashMap<>();
    private static final Map<String, Optional<String>> configurations = Map.of(
            "symbol/EURUSD/price", Optional.of("1.1578"),
            "symbol/EURGBP/price", Optional.of("0.8813"),
            "symbol/EURUSD/precision", Optional.of("5"),
            "symbol/EURGBP/precision", Optional.of("5")
    );

    DefaultServiceConfiguration() {
        services.put("order", Map.of("blotter", 8983));
        services.put("order", Map.of("execution", 8984));
        services.put("order", Map.of("orderbook", 8982));
        services.put("pricer", Map.of("pricing", 8981));
        services.put("matchingengine", Map.of("pricing", 8980));
        httpServices.put("order", "http://localhost:8080");
        httpServices.put("pricing", "http://localhost:8085");
    }

    @Override
    public void register(Service service, String host) {
        //nop
    }

    @Override
    public void discover(Disruptor<Message> disruptor, String... services) {

        var tcpNodes = new TcpNodes(disruptor);
        List<Node> nodes = new ArrayList<>();
        stream(services)
                .map(DefaultServiceConfiguration.services::get)
                .forEach(maps -> maps.forEach(map -> map.entrySet().stream()
                        .map(entry -> new Node(
                                entry.getKey(),
                                "localhost",
                                entry.getValue()
                        )).forEach(nodes::add))
                );
        tcpNodes.setHealthyNodes(nodes);

    }

    @Override
    public void update(Consumer<Map<String, Optional<String>>> consumer) {
        consumer.accept(configurations);

    }

    @Override
    public void discover(String... services) {

    }

    @Override
    public Optional<String> get(String service) {
        return ofNullable(httpServices.get(service));
    }
}

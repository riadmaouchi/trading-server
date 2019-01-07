package org.trading.discovery;

import com.lmax.disruptor.dsl.Disruptor;
import org.trading.messaging.Message;
import org.trading.discovery.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface ServiceConfiguration {

    void register(Service service, String host);

    void discover(Disruptor<Message> disruptor, String... services);

    void update(Consumer<Map<String, Optional<String>>> consumer);

    void discover(String... services);

    Optional<String> get(String service);

}

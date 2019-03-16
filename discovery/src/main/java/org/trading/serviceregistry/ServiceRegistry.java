package org.trading.serviceregistry;

import com.lmax.disruptor.dsl.Disruptor;
import org.trading.MessageProvider;
import org.trading.discovery.Service;
import org.trading.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ServiceRegistry {

    void register(Service service, String host, String serviceUrl);

    void discover(Disruptor<Message> disruptor, Supplier<List<MessageProvider.Message>> supplier, String... services);

    void update(Consumer<Map<String, Optional<String>>> consumer);

    void discover(String... services);

    Optional<String> get(String service);

}

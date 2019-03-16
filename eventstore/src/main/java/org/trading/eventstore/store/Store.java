package org.trading.eventstore.store;

import org.trading.eventstore.domain.Event;

import java.util.List;
import java.util.UUID;

public interface Store<T extends Event> extends AutoCloseable {

    void store(T event);

    List<T> loadEventStream(UUID aggregateId);
}

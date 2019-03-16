package org.trading.eventstore.store;

import com.google.common.collect.Multimap;
import org.trading.eventstore.domain.DomainEvent;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.LinkedHashMultimap.create;

public class InMemoryEventStore implements Store<DomainEvent> {

    private final Multimap<UUID, DomainEvent> store = create();

    @Override
    public void store(DomainEvent event) {
        store.put(event.getAggregateId(), event);
    }

    @Override
    public List<DomainEvent> loadEventStream(UUID aggregateId) {
        return List.copyOf(store.get(aggregateId));
    }

    @Override
    public void close() throws Exception {
        // nop
    }
}

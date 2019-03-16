package org.trading.eventstore.store;

import org.trading.eventstore.domain.Aggregate;
import org.trading.eventstore.domain.AggregateRepository;
import org.trading.eventstore.domain.DomainEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EventStoreCache<T extends Aggregate> implements AggregateRepository<T> {

    private final EventBus eventBus;
    private final Store<DomainEvent> eventStore;
    private final AggregateRepository<T> repository;

    public EventStoreCache(EventBus eventBus,
                           Store<DomainEvent> eventStore,
                           AggregateRepository<T> repository) {
        this.eventBus = eventBus;
        this.eventStore = eventStore;
        this.repository = repository;
    }

    @Override
    public Optional<T> load(UUID id) {
        return repository.load(id).or(() -> {
            List<DomainEvent> events = eventStore.loadEventStream(id);
            T aggregate = repository.create();
            aggregate.fromEvents(events);
            repository.save(aggregate);
            return Optional.of(aggregate);
        });
    }

    @Override
    public T create() {
        return repository.create();
    }

    @Override
    public void save(T aggregate) {
        List<DomainEvent> uncommittedEvents = aggregate.getUncommittedEvents();
        uncommittedEvents.forEach(domainEvent -> {
            eventStore.store(domainEvent);
            eventBus.dispatch(domainEvent);
        });
        repository.save(aggregate);
    }
}

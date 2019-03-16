package org.trading.eventstore.store;

import org.trading.eventstore.domain.IDRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class IDRepositoryCache<T> implements IDRepository<T, UUID> {

    private final IDRepository<T, UUID> aggregates;
    private final Map<T, UUID> activeAggregates = new HashMap<>();
    private final ExecutorService executorService = newSingleThreadExecutor();

    public IDRepositoryCache(IDRepository<T, UUID> aggregates) {
        this.aggregates = aggregates;
    }

    @Override
    public void store(T key, UUID aggregateId) {
        activeAggregates.put(key, aggregateId);
        executorService.execute(() -> aggregates.store(key, aggregateId));
    }

    @Override
    public Optional<UUID> load(T key) {
        UUID id = activeAggregates.get(key);
        if (null == id) {
            aggregates.load(key).ifPresent(uuid -> activeAggregates.put(key, uuid));
        }
        return ofNullable(activeAggregates.get(key));
    }
}

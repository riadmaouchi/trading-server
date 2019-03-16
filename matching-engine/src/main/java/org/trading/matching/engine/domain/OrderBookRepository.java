package org.trading.matching.engine.domain;

import org.trading.eventstore.domain.AggregateFactory;
import org.trading.eventstore.domain.AggregateRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class OrderBookRepository implements AggregateRepository<OrderBook> {
    private final AggregateFactory<OrderBook> factory;
    private final Map<UUID, OrderBook> books = new HashMap<>();

    public OrderBookRepository(AggregateFactory<OrderBook> factory) {
        this.factory = factory;
    }

    @Override
    public Optional<OrderBook> load(UUID id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public OrderBook create() {
        return factory.create();
    }

    @Override
    public void save(OrderBook orderBook) {
        books.put(orderBook.getId(), orderBook);
    }

}

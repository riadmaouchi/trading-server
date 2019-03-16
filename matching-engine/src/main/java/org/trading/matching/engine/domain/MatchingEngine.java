package org.trading.matching.engine.domain;

import org.slf4j.Logger;
import org.trading.api.message.SubmitOrder;
import org.trading.eventstore.command.Command;
import org.trading.eventstore.command.CommandProcessor;
import org.trading.eventstore.command.Processor;
import org.trading.eventstore.domain.Aggregate;
import org.trading.eventstore.domain.AggregateRepository;
import org.trading.eventstore.domain.IDRepository;
import org.trading.matching.engine.api.ExchangeRequestListener;
import org.trading.matching.engine.command.CreateOrderBook;

import java.util.Optional;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class MatchingEngine implements ExchangeRequestListener {
    private static final Logger LOGGER = getLogger(MatchingEngine.class);
    private final IDRepository<String, UUID> orderBooks;
    private final AggregateRepository<OrderBook> repository;
    private final Processor<OrderBook> processor;

    public MatchingEngine(IDRepository<String, UUID> orderBooks,
                          AggregateRepository<OrderBook> repository) {
        this.orderBooks = orderBooks;
        this.repository = repository;
        processor = new CommandProcessor<OrderBook>(
                id -> repository.load(id),
                repository::create,
                orderBook -> {
                    repository.save(orderBook);
                    orderBook.commitEvents();
                }

        );
    }

    @Override
    public void orderBookConfig(String symbol) {

        orderBooks.load(symbol).ifPresentOrElse(id -> repository.load(id)
                .ifPresentOrElse(Aggregate::commitEvents, () -> LOGGER.warn("Unknown aggregate for ID {}", id)), () -> {
            UUID id = UUID.randomUUID();
            orderBooks.store(symbol, id);
            Command<OrderBook> command = new CreateOrderBook(id);
            processor.processInitial(command);
        });
    }

    @Override
    public void submitOrder(SubmitOrder submitOrder) {

        orderBooks.load(submitOrder.symbol).ifPresentOrElse(aggregateId -> {
            Command<OrderBook> command = new org.trading.matching.engine.command.SubmitOrder(
                    aggregateId,
                    submitOrder
            );
            processor.process(command);
        }, () -> LOGGER.warn("Unable to load Order book for symbol {}", submitOrder.symbol));
    }

    public Optional<OrderBook> forSymbol(String symbol) {
        return orderBooks.load(symbol).flatMap(repository::load);
    }
}

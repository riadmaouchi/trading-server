package org.trading.matching.engine.command;

import org.trading.eventstore.command.Command;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

public class CreateOrderBook implements Command<OrderBook> {

    private final UUID id;

    public CreateOrderBook(UUID id) {
        this.id = id;
    }

    @Override
    public void execute(OrderBook orderBook) {
        orderBook.createOrderBook(id);
    }

    @Override
    public UUID getId() {
        return id;
    }
}

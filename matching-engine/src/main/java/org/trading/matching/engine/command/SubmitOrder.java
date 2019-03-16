package org.trading.matching.engine.command;

import org.trading.eventstore.command.Command;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

public class SubmitOrder implements Command<OrderBook> {
    private final UUID id;
    private final org.trading.api.message.SubmitOrder submitOrder;

    public SubmitOrder(UUID id, org.trading.api.message.SubmitOrder submitOrder) {
        this.id = id;
        this.submitOrder = submitOrder;
    }

    @Override
    public void execute(OrderBook orderBook) {
        orderBook.submitOrder(submitOrder);
    }

    @Override
    public UUID getId() {
        return id;
    }
}

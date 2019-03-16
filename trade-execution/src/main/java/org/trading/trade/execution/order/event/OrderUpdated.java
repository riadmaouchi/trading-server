package org.trading.trade.execution.order.event;

import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderUpdated {
    public final UUID id;
    public final LocalDateTime time;
    public final String broker;
    public final int requestedAmount;
    public final int leftAmount;
    public final int amount;
    public final Side direction;
    public final double limit;
    public final double price;
    public final String symbol;
    public final Status status;
    public final Type type;

    public OrderUpdated(UUID id,
                        LocalDateTime time,
                        String broker,
                        int requestedAmount,
                        int leftAmount,
                        int amount,
                        Side direction,
                        double limit,
                        double price,
                        String symbol,
                        Status status,
                        Type type) {
        this.id = id;
        this.time = time;
        this.broker = broker;
        this.requestedAmount = requestedAmount;
        this.leftAmount = leftAmount;
        this.amount = amount;
        this.direction = direction;
        this.limit = limit;
        this.price = price;
        this.symbol = symbol;
        this.status = status;
        this.type = type;
    }


    public enum Status {
        SUBMITTING, WORKING, DONE, CANCELLED
    }


    public enum Type {
        LIMIT, MARKET
    }
}

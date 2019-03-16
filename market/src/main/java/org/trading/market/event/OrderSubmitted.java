package org.trading.market.event;

import org.trading.api.message.OrderType;
import org.trading.api.message.Side;

public class OrderSubmitted {
    public final String symbol;
    public final String broker;
    public final long amount;
    public final Side side;
    public final OrderType type;
    public final double price;

    public OrderSubmitted(String symbol,
                          String broker,
                          long amount,
                          Side side,
                          OrderType type,
                          double price) {
        this.symbol = symbol;
        this.broker = broker;
        this.amount = amount;
        this.side = side;
        this.type = type;
        this.price = price;
    }
}

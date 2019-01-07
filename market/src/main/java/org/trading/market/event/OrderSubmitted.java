package org.trading.market.event;

import org.trading.api.command.OrderType;
import org.trading.api.command.Side;

public class OrderSubmitted {
    public final String symbol;
    public final String broker;
    public final double amount;
    public final Side side;
    public final OrderType type;
    public final double price;

    public OrderSubmitted(String symbol,
                          String broker,
                          double amount,
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

package org.trading.trade.execution.esp.domain;

import org.trading.api.message.Side;

public class Trade {

    public final String id;
    public final String broker;
    public final int quantity;
    public final Side side;
    public final String symbol;
    public final double price;

    public Trade(String id, String broker, int quantity, Side side, String symbol, double price) {
        this.id = id;
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.symbol = symbol;
        this.price = price;
    }
}

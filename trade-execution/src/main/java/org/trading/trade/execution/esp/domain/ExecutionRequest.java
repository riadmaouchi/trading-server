package org.trading.trade.execution.esp.domain;

import org.trading.api.command.Side;

public class ExecutionRequest {

    public final String broker;
    public final int quantity;
    public final Side side;
    public final String symbol;
    public final double price;

    public ExecutionRequest(String broker, int quantity, Side side, String symbol, double price) {
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.symbol = symbol;
        this.price = price;
    }
}

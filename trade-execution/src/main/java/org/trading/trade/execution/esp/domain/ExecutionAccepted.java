package org.trading.trade.execution.esp.domain;

import org.trading.api.command.Side;

import java.time.LocalDateTime;

public class ExecutionAccepted {
    public final LocalDateTime tradeDate;
    public final String id;
    public final String symbol;
    public final Side side;
    public final double price;
    public final int quantity;
    public final String broker;

    public ExecutionAccepted(LocalDateTime tradeDate,
                             String id,
                             String symbol,
                             Side side,
                             double price,
                             int quantity,
                             String broker) {
        this.tradeDate = tradeDate;
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.broker = broker;
    }
}

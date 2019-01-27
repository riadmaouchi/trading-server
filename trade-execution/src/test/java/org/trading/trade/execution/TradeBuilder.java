package org.trading.trade.execution;

import org.trading.api.command.Side;
import org.trading.trade.execution.esp.domain.ExecutionRequest;
import org.trading.trade.execution.esp.domain.Trade;

import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;

public final class TradeBuilder {
    private String id = "ID";
    private String symbol = "EURUSD";
    private String broker = "Broker";
    private Side side = BUY;
    private int quantity = 10;
    private double price = 1.34;

    private TradeBuilder() {
    }

    public static TradeBuilder aTrade() {
        return new TradeBuilder();
    }

    public static TradeBuilder aBuyTrade() {
        return new TradeBuilder().withSide(BUY);
    }

    public static TradeBuilder aSellTrade() {
        return new TradeBuilder().withSide(SELL);
    }

    public Trade build() {
        return new Trade(
                id,
                broker,
                quantity,
                side,
                symbol,
                price
        );
    }

    public TradeBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public TradeBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public TradeBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public TradeBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public TradeBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }
}

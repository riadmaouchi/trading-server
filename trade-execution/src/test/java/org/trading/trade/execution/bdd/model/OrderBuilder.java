package org.trading.trade.execution.bdd.model;

import org.trading.api.command.Side;

import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;

public final class OrderBuilder {
    private String symbol = "EURUSD";
    private String broker = "Broker";
    private Side side = BUY;
    private String quantity = "10";
    private String price = "1.34";

    private OrderBuilder() {
    }

    public static OrderBuilder aLimitOrder() {
        return new OrderBuilder();
    }

    public static OrderBuilder aBuyLimitOrder() {
        return new OrderBuilder().withSide(BUY);
    }

    public static OrderBuilder aSellLimitOrder() {
        return new OrderBuilder().withSide(SELL);
    }

    public Order build() {
        return new Order(
                symbol,
                broker,
                side,
                quantity,
                price
        );
    }

    public OrderBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public OrderBuilder broker(final String broker) {
        this.broker = broker;
        return this;
    }

    public OrderBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public OrderBuilder quantity(final String quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderBuilder price(final String price) {
        this.price = price;
        return this;
    }
}

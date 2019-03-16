package org.trading.api;

import org.trading.api.message.Side;
import org.trading.api.message.SubmitOrder;

public final class SubmitOrderBuilder {
    private String symbol = "EURUSD";
    private String broker = "Broker";
    private int amount = 1_000;
    private Side side = Side.BUY;

    private SubmitOrderBuilder() {
    }

    public static SubmitOrderBuilder aMarketOrder() {
        return new SubmitOrderBuilder();
    }

    public static SubmitOrder aSubmitOrder() {
        return aMarketOrder().build();
    }

    public SubmitOrder build() {
        return SubmitOrder.aSubmitMarketOrder(
                symbol,
                broker,
                amount,
                side
        );
    }

    public SubmitOrderBuilder withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public SubmitOrderBuilder withBroker(String broker) {
        this.broker = broker;
        return this;
    }

    public SubmitOrderBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public SubmitOrderBuilder withSide(Side side) {
        this.side = side;
        return this;
    }
}

package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.MarketOrderPlaced;
import org.trading.Side;

public final class MarketOrderPlacedBuilder {
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String id = "00000000-0000-0000-0000-000000000001";
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";

    private MarketOrderPlacedBuilder() {
    }

    public static MarketOrderPlacedBuilder aMarketOrderPlaced() {
        return new MarketOrderPlacedBuilder();
    }

    public MarketOrderPlaced build() {
        return MarketOrderPlaced.newBuilder()
                .setId(id)
                .setQuantity(quantity)
                .setTime(time)
                .setBroker(broker)
                .setSymbol(symbol)
                .setBroker(broker)
                .setSide(side)
                .build();
    }

    public MarketOrderPlacedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public MarketOrderPlacedBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public MarketOrderPlacedBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public MarketOrderPlacedBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public MarketOrderPlacedBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public MarketOrderPlacedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }
}

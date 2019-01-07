package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.LimitOrderPlaced;
import org.trading.Side;

public final class LimitOrderPlacedBuilder {
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String id = "00000000-0000-0000-0000-000000000001";
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";
    private double price = 1.47843;

    private LimitOrderPlacedBuilder() {
    }

    public static LimitOrderPlacedBuilder aLimitOrderPlaced() {
        return new LimitOrderPlacedBuilder();
    }


    public LimitOrderPlaced build() {
        return LimitOrderPlaced.newBuilder()
                .setId(id)
                .setLimit(price)
                .setQuantity(quantity)
                .setTime(time)
                .setBroker(broker)
                .setSymbol(symbol)
                .setBroker(broker)
                .setSide(side)
                .build();
    }

    public LimitOrderPlacedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public LimitOrderPlacedBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public LimitOrderPlacedBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public LimitOrderPlacedBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public LimitOrderPlacedBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public LimitOrderPlacedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public LimitOrderPlacedBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }
}

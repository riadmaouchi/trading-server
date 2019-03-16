package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.MessageProvider.MarketOrderAccepted;
import org.trading.MessageProvider.Side;

public final class MarketOrderAcceptedBuilder {
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String id = "00000000-0000-0000-0000-000000000001";
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";

    private MarketOrderAcceptedBuilder() {
    }

    public static MarketOrderAcceptedBuilder aMarketOrderAccepted() {
        return new MarketOrderAcceptedBuilder();
    }

    public MarketOrderAccepted build() {
        return MarketOrderAccepted.newBuilder()
                .setId(id)
                .setQuantity(quantity)
                .setTime(time)
                .setBroker(broker)
                .setSymbol(symbol)
                .setBroker(broker)
                .setSide(side)
                .build();
    }

    public MarketOrderAcceptedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public MarketOrderAcceptedBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public MarketOrderAcceptedBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public MarketOrderAcceptedBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public MarketOrderAcceptedBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public MarketOrderAcceptedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }
}

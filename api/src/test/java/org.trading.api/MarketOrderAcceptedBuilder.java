package org.trading.api;

import org.trading.api.event.MarketOrderAccepted;
import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class MarketOrderAcceptedBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
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
        return new MarketOrderAccepted(
                id,
                broker,
                quantity,
                side,
                symbol,
                time
        );
    }

    public MarketOrderAcceptedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public MarketOrderAcceptedBuilder withId(final UUID id) {
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

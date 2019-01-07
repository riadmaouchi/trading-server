package org.trading.api;

import org.trading.api.command.Side;
import org.trading.api.event.MarketOrderPlaced;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class MarketOrderPlacedBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
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
        return new MarketOrderPlaced(
                id,
                broker,
                quantity,
                side,
                symbol,
                time
        );
    }

    public MarketOrderPlacedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public MarketOrderPlacedBuilder withId(final UUID id) {
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

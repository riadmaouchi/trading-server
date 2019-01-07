package org.trading.api;

import org.trading.api.command.Side;
import org.trading.api.event.LimitOrderPlaced;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class LimitOrderPlacedBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
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
        return new LimitOrderPlaced(
                id,
                time,
                broker,
                quantity,
                side,
                price,
                symbol
        );
    }

    public LimitOrderPlacedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public LimitOrderPlacedBuilder withId(final UUID id) {
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

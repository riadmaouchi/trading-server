package org.trading.matching.engine;

import org.trading.api.message.Side;
import org.trading.matching.engine.domain.MarketOrder;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class MarketOrderBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";

    private MarketOrderBuilder() {
    }

    public static MarketOrderBuilder aMarketOrder() {
        return new MarketOrderBuilder();
    }

    public MarketOrder build() {
        return new MarketOrder(
                id,
                symbol,
                broker,
                quantity,
                side,
                time
        );
    }

    public MarketOrderBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public MarketOrderBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public MarketOrderBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public MarketOrderBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public MarketOrderBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public MarketOrderBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }
}

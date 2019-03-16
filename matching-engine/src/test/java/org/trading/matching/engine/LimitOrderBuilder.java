package org.trading.matching.engine;

import org.trading.api.message.Side;
import org.trading.matching.engine.domain.LimitOrder;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class LimitOrderBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
    private String broker = "Broker";
    private int quantity = 1_000_000;
    private Side side = Side.BUY;
    private String symbol = "EURUSD";
    private double limit = 1.47843;

    private LimitOrderBuilder() {
    }

    public static LimitOrderBuilder aLimitOrder() {
        return new LimitOrderBuilder();
    }

    public LimitOrder build() {
        return new LimitOrder(
                id,
                symbol,
                broker,
                quantity,
                side,
                limit,
                time
        );
    }

    public LimitOrderBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public LimitOrderBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public LimitOrderBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public LimitOrderBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public LimitOrderBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public LimitOrderBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public LimitOrderBuilder withLimit(final double limit) {
        this.limit = limit;
        return this;
    }

}

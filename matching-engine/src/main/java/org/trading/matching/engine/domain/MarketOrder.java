package org.trading.matching.engine.domain;

import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public final class MarketOrder extends Order {

    public MarketOrder(UUID id,
                       String symbol,
                       String broker,
                       int quantity,
                       Side side,
                       LocalDateTime time) {
        super(id, broker, quantity, side, symbol, time);
    }

    @Override
    public boolean crossesAt(double price) {
        return true;
    }

    @Override
    public <R> R accept(OrderVisitor<R> visitor) {
        return visitor.visitMarketOrder(this);
    }
}

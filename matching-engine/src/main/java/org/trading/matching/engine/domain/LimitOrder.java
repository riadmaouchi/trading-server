package org.trading.matching.engine.domain;

import org.trading.api.command.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public final class LimitOrder extends Order {
    public final double limit;

    public LimitOrder(UUID id,
                      String symbol,
                      String broker,
                      int quantity,
                      Side side,
                      double limit,
                      LocalDateTime time) {
        super(id, broker, quantity, side, symbol, time);
        this.limit = limit;
    }

    @Override
    public boolean crossesAt(double price) {
        return side.accept(new Side.SideVisitor<>() {
            @Override
            public Boolean visitBuy() {
                return price <= limit;
            }

            @Override
            public Boolean visitSell() {
                return price >= limit;
            }
        });
    }

    @Override
    public <R> R accept(OrderVisitor<R> visitor) {
        return visitor.visitLimitOrder(this);
    }

}

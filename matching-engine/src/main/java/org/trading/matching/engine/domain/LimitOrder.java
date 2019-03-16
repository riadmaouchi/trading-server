package org.trading.matching.engine.domain;

import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
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


    static Comparator<LimitOrder> getBuyComparator() {
        return Comparator.<LimitOrder>comparingDouble(o -> -o.limit)
                .thenComparing(o -> o.id);
    }

    static Comparator<LimitOrder> getSellComparator() {
        return Comparator.<LimitOrder>comparingDouble(o -> +o.limit)
                .thenComparing(o -> o.id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LimitOrder)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        LimitOrder that = (LimitOrder) other;
        return Double.compare(that.limit, limit) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), limit);
    }
}

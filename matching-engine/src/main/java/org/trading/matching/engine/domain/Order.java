package org.trading.matching.engine.domain;

import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class Order {
    public final UUID id;
    public final String broker;
    public final int quantity;
    private int openQuantity;
    public final Side side;
    public final String symbol;
    public final LocalDateTime time;

    Order(UUID id, String broker, int quantity, Side side, String symbol, LocalDateTime time) {
        this.id = id;
        this.broker = broker;
        this.quantity = quantity;
        this.openQuantity = quantity;
        this.side = side;
        this.symbol = symbol;
        this.time = time;
    }

    boolean isClosed() {
        return openQuantity == 0;
    }

    void decreasedBy(int quantity) {
        this.openQuantity -= quantity;
    }

    public abstract boolean crossesAt(double price);

    public abstract <R> R accept(OrderVisitor<R> visitor);

    public interface OrderVisitor<R> {

        R visitMarketOrder(MarketOrder marketOrder);

        R visitLimitOrder(LimitOrder limitOrder);
    }

    public int getOpenQuantity() {
        return openQuantity;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order)) {
            return false;
        }
        Order that = (Order) other;
        return quantity == that.quantity &&
                openQuantity == that.openQuantity &&
                Objects.equals(id, that.id) &&
                Objects.equals(broker, that.broker) &&
                side == that.side &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, broker, quantity, openQuantity, side, symbol, time);
    }
}

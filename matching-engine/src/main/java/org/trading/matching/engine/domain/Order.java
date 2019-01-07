package org.trading.matching.engine.domain;

import org.trading.api.command.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Order {
    final UUID id;
    public final String broker;
    public final int quantity;
    private int openQuantity;
    private int executedQuantity;
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
        this.executedQuantity += quantity;
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

    public int getExecutedQuantity() {
        return executedQuantity;
    }

}

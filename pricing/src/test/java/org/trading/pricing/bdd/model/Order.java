package org.trading.pricing.bdd.model;

import com.google.common.base.MoreObjects;
import org.trading.api.command.Side;

import java.util.Objects;

public class Order {
    public final String symbol;
    public final String broker;
    public final Side side;
    public final String quantity;
    public final String price;

    public Order(String symbol,
                 String broker,
                 Side side,
                 String quantity,
                 String price) {
        this.symbol = symbol;
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.price = price;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order)) {
            return false;
        }
        Order order = (Order) other;
        return Objects.equals(symbol, order.symbol) &&
                Objects.equals(broker, order.broker) &&
                side == order.side &&
                Objects.equals(quantity, order.quantity) &&
                Objects.equals(price, order.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, broker, side, quantity, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("broker", broker)
                .add("side", side)
                .add("quantity", quantity)
                .add("price", price)
                .toString();
    }
}

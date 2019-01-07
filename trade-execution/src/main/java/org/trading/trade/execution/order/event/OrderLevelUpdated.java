package org.trading.trade.execution.order.event;

import com.google.common.base.MoreObjects;
import org.trading.api.command.Side;

public class OrderLevelUpdated {
    public final String symbol;
    public final Side side;
    public final int quantity;
    public final double price;

    public OrderLevelUpdated(String symbol, Side side, int quantity, double price) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("side", side)
                .add("quantity", quantity)
                .add("price", price)
                .toString();
    }
}

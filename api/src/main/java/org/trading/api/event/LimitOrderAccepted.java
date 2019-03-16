package org.trading.api.event;

import com.google.common.base.MoreObjects;
import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public class LimitOrderAccepted {

    public final UUID id;
    public final LocalDateTime time;
    public final String broker;
    public final int quantity;
    public final Side side;
    public final double price;
    public final String symbol;

    public LimitOrderAccepted(UUID id,
                              LocalDateTime time,
                              String broker,
                              int quantity,
                              Side side,
                              double price,
                              String symbol) {
        this.id = id;
        this.time = time;
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.price = price;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("broker", broker)
                .add("quantity", quantity)
                .add("side", side)
                .add("price", price)
                .add("symbol", symbol)
                .add("time", time)
                .toString();
    }
}

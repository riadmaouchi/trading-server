package org.trading.api.event;

import com.google.common.base.MoreObjects;
import org.trading.api.message.Side;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrderAccepted {
    public final UUID id;
    public final String broker;
    public final int quantity;
    public final Side side;
    public final String symbol;
    public final LocalDateTime time;

    public MarketOrderAccepted(UUID id,
                               String broker,
                               int quantity,
                               Side side,
                               String symbol,
                               LocalDateTime time) {
        this.id = id;
        this.broker = broker;
        this.quantity = quantity;
        this.side = side;
        this.symbol = symbol;
        this.time = time;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("broker", broker)
                .add("quantity", quantity)
                .add("side", side)
                .add("symbol", symbol)
                .add("time", time)
                .toString();
    }
}

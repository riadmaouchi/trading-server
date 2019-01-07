package org.trading.api.event;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.UUID;

public class TradeExecuted {
    public final UUID buyingId;
    public final String buyingBroker;
    public final UUID sellingId;
    public final String sellingBroker;
    public final int quantity;
    public final double price;
    public final double buyingLimit;
    public final double sellingLimit;
    public final LocalDateTime time;
    public final String symbol;

    public TradeExecuted(UUID buyingId,
                         String buyingBroker,
                         UUID sellingId,
                         String sellingBroker,
                         int quantity,
                         double price,
                         double buyingLimit,
                         double sellingLimit,
                         LocalDateTime time,
                         String symbol) {
        this.buyingId = buyingId;
        this.buyingBroker = buyingBroker;
        this.sellingId = sellingId;
        this.sellingBroker = sellingBroker;
        this.price = price;
        this.quantity = quantity;
        this.buyingLimit = buyingLimit;
        this.sellingLimit = sellingLimit;
        this.time = time;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("buyingId", buyingId)
                .add("buyingBroker", buyingBroker)
                .add("sellingId", sellingId)
                .add("sellingBroker", sellingBroker)
                .add("quantity", quantity)
                .add("price", price)
                .add("buyingLimit", buyingLimit)
                .add("sellingLimit", sellingLimit)
                .add("time", time)
                .add("symbol", symbol)
                .toString();
    }
}

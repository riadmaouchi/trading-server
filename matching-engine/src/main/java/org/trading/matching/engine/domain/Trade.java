package org.trading.matching.engine.domain;

import com.google.common.base.MoreObjects;
import org.trading.api.message.OrderType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Trade {

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
    public final OrderType buyingOrderType;
    public final OrderType sellingOrderType;

    public Trade(UUID buyingId,
                 String buyingBroker,
                 UUID sellingId,
                 String sellingBroker,
                 int quantity,
                 double price,
                 double buyingLimit,
                 double sellingLimit,
                 LocalDateTime time,
                 String symbol,
                 OrderType buyingOrderType,
                 OrderType sellingOrderType) {
        this.buyingId = buyingId;
        this.buyingBroker = buyingBroker;
        this.sellingId = sellingId;
        this.sellingBroker = sellingBroker;
        this.quantity = quantity;
        this.price = price;
        this.buyingLimit = buyingLimit;
        this.sellingLimit = sellingLimit;
        this.time = time;
        this.symbol = symbol;
        this.buyingOrderType = buyingOrderType;
        this.sellingOrderType = sellingOrderType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Trade)) {
            return false;
        }
        Trade that = (Trade) other;
        return quantity == that.quantity &&
                Double.compare(that.price, price) == 0 &&
                Double.compare(that.buyingLimit, buyingLimit) == 0 &&
                Double.compare(that.sellingLimit, sellingLimit) == 0 &&
                Objects.equals(buyingId, that.buyingId) &&
                Objects.equals(buyingBroker, that.buyingBroker) &&
                Objects.equals(sellingId, that.sellingId) &&
                Objects.equals(sellingBroker, that.sellingBroker) &&
                Objects.equals(time, that.time) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(sellingOrderType, that.sellingOrderType) &&
                Objects.equals(buyingOrderType, that.buyingOrderType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                buyingId,
                buyingBroker,
                sellingId,
                sellingBroker,
                quantity,
                price,
                buyingLimit,
                sellingLimit,
                time,
                symbol,
                sellingOrderType,
                buyingOrderType
        );
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
                .add("sellingOrderType", sellingOrderType)
                .add("buyingOrderType", buyingOrderType)
                .toString();
    }
}


package org.trading.matching.engine;


import org.trading.api.message.OrderType;
import org.trading.matching.engine.domain.Trade;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class TradeBuilder {
    private UUID buyingId = new UUID(0, 1);
    private String buyingBroker = "Buying Broker";
    private UUID sellingId = new UUID(1, 2);
    private String sellingBroker = "Selling Broker";
    private int quantity = 1_000_000;
    private double price = 1.183726;
    private double buyingLimit = 1.183729;
    private double sellingLimit = 1.183726;
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private String symbol = "EURUSD";
    private OrderType buyingOrderType = OrderType.LIMIT;
    private OrderType sellingOrderType = OrderType.LIMIT;

    private TradeBuilder() {
    }

    public static TradeBuilder aTrade() {
        return new TradeBuilder();
    }

    public Trade build() {
        return new Trade(
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
                buyingOrderType,
                sellingOrderType
        );
    }

    public TradeBuilder withBuyingId(final UUID buyingId) {
        this.buyingId = buyingId;
        return this;
    }

    public TradeBuilder withBuyingBroker(final String buyingBroker) {
        this.buyingBroker = buyingBroker;
        return this;
    }

    public TradeBuilder withSellingId(final UUID sellingId) {
        this.sellingId = sellingId;
        return this;
    }

    public TradeBuilder withSellingBroker(final String sellingBroker) {
        this.sellingBroker = sellingBroker;
        return this;
    }

    public TradeBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public TradeBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }

    public TradeBuilder withBuyingLimit(final double buyingLimit) {
        this.buyingLimit = buyingLimit;
        return this;
    }

    public TradeBuilder withSellingLimit(final double sellingLimit) {
        this.sellingLimit = sellingLimit;
        return this;
    }

    public TradeBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public TradeBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public TradeBuilder withBuyingOrderType(final OrderType buyingOrderType) {
        this.buyingOrderType = buyingOrderType;
        return this;
    }

    public TradeBuilder withSellingOrderType(final OrderType sellingOrderType) {
        this.sellingOrderType = sellingOrderType;
        return this;
    }

}

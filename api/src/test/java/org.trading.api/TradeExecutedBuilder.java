package org.trading.api;

import org.trading.api.event.TradeExecuted;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class TradeExecutedBuilder {
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

    private TradeExecutedBuilder() {
    }

    public static TradeExecutedBuilder aTradeExecuted() {
        return new TradeExecutedBuilder();
    }

    public TradeExecuted build() {
        return new TradeExecuted(
                buyingId,
                buyingBroker,
                sellingId,
                sellingBroker,
                quantity,
                price,
                buyingLimit,
                sellingLimit,
                time,
                symbol
        );
    }

    public TradeExecutedBuilder withBuyingId(final UUID buyingId) {
        this.buyingId = buyingId;
        return this;
    }

    public TradeExecutedBuilder withBuyingBroker(final String buyingBroker) {
        this.buyingBroker = buyingBroker;
        return this;
    }

    public TradeExecutedBuilder withSellingId(final UUID sellingId) {
        this.sellingId = sellingId;
        return this;
    }

    public TradeExecutedBuilder withSellingBroker(final String sellingBroker) {
        this.sellingBroker = sellingBroker;
        return this;
    }

    public TradeExecutedBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public TradeExecutedBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }

    public TradeExecutedBuilder withBuyingLimit(final double buyingLimit) {
        this.buyingLimit = buyingLimit;
        return this;
    }

    public TradeExecutedBuilder withSellingLimit(final double sellingLimit) {
        this.sellingLimit = sellingLimit;
        return this;
    }

    public TradeExecutedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public TradeExecutedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

}

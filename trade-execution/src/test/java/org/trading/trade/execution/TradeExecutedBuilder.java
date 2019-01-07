package org.trading.trade.execution;

import org.trading.api.event.TradeExecuted;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;

public final class TradeExecutedBuilder {

    private UUID buyingId = new UUID(0,1);
    private String buyingBroker = "Buyer";
    private UUID sellingId = new UUID(0,2);
    private String sellingBroker = "Seller";
    private int quantity = 100;
    private double price = 15.6;
    private double buyingLimit = 14.9;
    private double sellingLimit = 13.7;
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 17);
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

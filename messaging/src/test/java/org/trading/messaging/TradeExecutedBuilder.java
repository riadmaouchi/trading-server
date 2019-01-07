package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.TradeExecuted;

public final class TradeExecutedBuilder {
    private String buyingId = "00000000-0000-0000-0000-000000000001";
    private String buyingBroker = "Buying Broker";
    private String sellingId = "00000000-0000-0001-0000-000000000002";
    private String sellingBroker = "Selling Broker";
    private int quantity = 1_000_000;
    private double price = 1.183726;
    private double buyingLimit = 1.183729;
    private double sellingLimit = 1.183726;
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String symbol = "EURUSD";

    private TradeExecutedBuilder() {
    }

    public static TradeExecutedBuilder aTradeExecuted() {
        return new TradeExecutedBuilder();
    }

    public TradeExecuted build() {
        return TradeExecuted.newBuilder()
                .setBuyingId(buyingId)
                .setBuyingBroker(buyingBroker)
                .setSellingId(sellingId)
                .setSellingBroker(sellingBroker)
                .setQuantity(quantity)
                .setPrice(price)
                .setBuyingLimit(buyingLimit)
                .setSellingLimit(sellingLimit)
                .setTime(time)
                .setSymbol(symbol)
                .build();
    }

    public TradeExecutedBuilder withBuyingId(final String buyingId) {
        this.buyingId = buyingId;
        return this;
    }

    public TradeExecutedBuilder withBuyingBroker(final String buyingBroker) {
        this.buyingBroker = buyingBroker;
        return this;
    }

    public TradeExecutedBuilder withSellingId(final String sellingId) {
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

    public TradeExecutedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public TradeExecutedBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

}

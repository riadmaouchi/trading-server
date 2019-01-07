package org.trading.trade.execution;

import net.minidev.json.JSONObject;

public final class SubmitOrderBuilder {
    private long id = 1L;
    private String symbol = "EURUSD";
    private String broker = "A";
    private double amount = 14.9;
    private String side = "BUY";
    private String type = "LIMIT";
    private double price = 13.9;

    private SubmitOrderBuilder() {
    }

    public static SubmitOrderBuilder aSubmitOrder() {
        return new SubmitOrderBuilder();
    }

    public JSONObject build(){
        final JSONObject placeOrderRequest = new JSONObject();
        placeOrderRequest.put("id", id);
        placeOrderRequest.put("symbol", symbol);
        placeOrderRequest.put("broker", broker);
        placeOrderRequest.put("amount", amount);
        placeOrderRequest.put("side", side);
        placeOrderRequest.put("type", type);
        placeOrderRequest.put("price", price);
        return placeOrderRequest;
    }

    public SubmitOrderBuilder withId(final long id) {
        this.id = id;
        return this;
    }

    public SubmitOrderBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public SubmitOrderBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public SubmitOrderBuilder withAmount(final double quantity) {
        this.amount = quantity;
        return this;
    }

    public SubmitOrderBuilder withSide(final String side) {
        this.side = side;
        return this;
    }

    public SubmitOrderBuilder withType(final String orderType) {
        this.type = orderType;
        return this;
    }

    public SubmitOrderBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }
}

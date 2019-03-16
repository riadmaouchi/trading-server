package org.trading.messaging;

import org.trading.MessageProvider.OrderType;
import org.trading.MessageProvider.Side;
import org.trading.MessageProvider.SubmitOrder;

import static org.trading.MessageProvider.OrderType.LIMIT;
import static org.trading.MessageProvider.OrderType.MARKET;

public final class SubmitOrderBuilder {
    private String symbol = "EURUSD";
    private String broker = "Broker";
    private int amount = 1_000;
    private Side side = Side.BUY;
    private OrderType orderType = LIMIT;
    private double price = 1.33234;

    private SubmitOrderBuilder() {
    }

    public static SubmitOrderBuilder aMarketOrder() {
        return new SubmitOrderBuilder().withOrderType(MARKET);
    }

    public static SubmitOrderBuilder aLimitOrder() {
        return new SubmitOrderBuilder().withOrderType(LIMIT);
    }

    public SubmitOrder build() {
        return SubmitOrder.newBuilder()
                .setAmount(amount)
                .setBroker(broker)
                .setPrice(price)
                .setSymbol(symbol)
                .setBroker(broker)
                .setSide(side)
                .setOrderType(orderType)
                .build();
    }

    public SubmitOrderBuilder withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public SubmitOrderBuilder withBroker(String broker) {
        this.broker = broker;
        return this;
    }

    public SubmitOrderBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public SubmitOrderBuilder withSide(Side side) {
        this.side = side;
        return this;
    }

    private SubmitOrderBuilder withOrderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public SubmitOrderBuilder withPrice(double price) {
        this.price = price;
        return this;
    }
}

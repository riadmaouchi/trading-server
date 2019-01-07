package org.trading.trade.execution;

import org.trading.api.command.Side;
import org.trading.trade.execution.esp.domain.ExecutionRequest;

import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;

public final class ExecutionRequestBuilder {
    private String symbol = "EURUSD";
    private String broker = "Broker";
    private Side side = BUY;
    private int quantity = 10;
    private double price = 1.34;

    private ExecutionRequestBuilder() {
    }

    public static ExecutionRequestBuilder anExecutionRequest() {
        return new ExecutionRequestBuilder();
    }

    public static ExecutionRequestBuilder aBuyExecutionRequest() {
        return new ExecutionRequestBuilder().withSide(BUY);
    }

    public static ExecutionRequestBuilder aSellExecutionRequest() {
        return new ExecutionRequestBuilder().withSide(SELL);
    }

    public ExecutionRequest build() {
        return new ExecutionRequest(
                broker,
                quantity,
                side,
                symbol,
                price
        );
    }

    public ExecutionRequestBuilder withSymbol(final String symbol) {
        this.symbol = symbol;
        return this;
    }

    public ExecutionRequestBuilder withBroker(final String broker) {
        this.broker = broker;
        return this;
    }

    public ExecutionRequestBuilder withSide(final Side side) {
        this.side = side;
        return this;
    }

    public ExecutionRequestBuilder withQuantity(final int quantity) {
        this.quantity = quantity;
        return this;
    }

    public ExecutionRequestBuilder withPrice(final double price) {
        this.price = price;
        return this;
    }
}

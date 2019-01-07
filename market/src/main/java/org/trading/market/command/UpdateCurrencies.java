package org.trading.market.command;

public class UpdateCurrencies {
    public final String symbol;
    public final double price;

    public UpdateCurrencies(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }
}

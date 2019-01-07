package org.trading.market.command;

import com.google.common.base.MoreObjects;

public class LastTradePrice {
    public final String symbol;
    public final double price;

    public LastTradePrice(String symbol,
                          double price) {
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("price", price)
                .toString();
    }
}

package org.trading.market.command;

import com.google.common.base.MoreObjects;

public class UpdatePrecision {
    public final String symbol;
    public final int precision;

    public UpdatePrecision(String symbol, int precision) {
        this.symbol = symbol;
        this.precision = precision;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("precision", precision)
                .toString();
    }
}

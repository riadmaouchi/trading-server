package org.trading.trade.execution.order.event;

import java.time.LocalDateTime;

public class LastTradeExecuted {
    public final String symbol;
    public final double lastPrice;
    public final int lastQuantity;
    public final LocalDateTime time;
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    public LastTradeExecuted(String symbol,
                             double lastPrice,
                             int lastQuantity,
                             LocalDateTime time,
                             double open,
                             double high,
                             double low,
                             double close) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.lastQuantity = lastQuantity;
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public LastTradeExecuted(String symbol,
                             double lastPrice,
                             int lastQuantity,
                             LocalDateTime time) {
        this(symbol, lastPrice, lastQuantity, time, 0, 0, 0, 0);
    }
}

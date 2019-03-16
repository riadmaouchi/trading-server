package org.trading.api.message;

import static java.lang.Double.NaN;
import static org.trading.api.message.OrderType.LIMIT;
import static org.trading.api.message.OrderType.MARKET;

public class SubmitOrder {
    public final String symbol;
    public final String broker;
    public final int amount;
    public final Side side;
    public final OrderType orderType;
    public final double limit;

    private SubmitOrder(String symbol,
                        String broker,
                        int amount,
                        Side side,
                        OrderType orderType,
                        double limit) {
        this.symbol = symbol;
        this.broker = broker;
        this.amount = amount;
        this.side = side;
        this.orderType = orderType;
        this.limit = limit;
    }

    public static SubmitOrder aSubmitMarketOrder(String symbol, String broker, int amount, Side side) {
        return new SubmitOrder(symbol, broker, amount, side, MARKET, NaN);
    }

    public static SubmitOrder aSubmitLimitOrder(String symbol, String broker, int amount, Side side, double price) {
        return new SubmitOrder(symbol, broker, amount, side, LIMIT, price);
    }
}


package org.trading.api;

import org.trading.EventType;

public interface MessageTypeVisitor<T, R> {

    R visitSubmitOrder(T t);

    R visitLimitOrderPlaced(T t);

    R visitMarketOrderPlaced(T t);

    R visitTradeExecuted(T t);

    R visitUnknownValue(T t);

    default R visit(EventType eventType, T t) {
        R result;

        switch (eventType) {
            case SUBMIT_ORDER:
                result = visitSubmitOrder(t);
                break;
            case LIMIT_ORDER_PLACED:
                result = visitLimitOrderPlaced(t);
                break;
            case MARKET_ORDER_PLACED:
                result = visitMarketOrderPlaced(t);
                break;
            case TRADE_EXECUTED:
                result = visitTradeExecuted(t);
                break;
            default:
                result = visitUnknownValue(t);
                break;
        }
        return result;
    }
}

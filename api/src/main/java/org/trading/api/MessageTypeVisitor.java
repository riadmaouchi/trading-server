package org.trading.api;

import org.trading.MessageProvider.EventType;

public interface MessageTypeVisitor<T, R> {

    R visitSubmitOrder(T t);

    R visitLimitOrderAccepted(T t);

    R visitMarketOrderAccepted(T t);

    R visitTradeExecuted(T t);

    R visitUnknownValue(T t);

    R visitMarketOrderRejected(T t);

    default R visit(EventType eventType, T t) {
        R result;

        switch (eventType) {
            case SUBMIT_ORDER:
                result = visitSubmitOrder(t);
                break;
            case LIMIT_ORDER_ACCEPTED:
                result = visitLimitOrderAccepted(t);
                break;
            case MARKET_ORDER_ACCEPTED:
                result = visitMarketOrderAccepted(t);
                break;
            case MARKET_ORDER_REJECTED:
                result = visitMarketOrderRejected(t);
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

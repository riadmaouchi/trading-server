package org.trading.api;

import org.trading.OrderType;

public interface OrderTypeVisitor<T, R> {

    R visitLimitOrder(T t);

    R visitMarketOrder(T t);

    R visitUnknownValue(T t);

    default R visit(OrderType orderType, T t) {
        R result;

        switch (orderType) {
            case LIMIT:
                result = visitLimitOrder(t);
                break;
            case MARKET:
                result = visitMarketOrder(t);
                break;
            default:
                result = visitUnknownValue(t);
                break;
        }
        return result;
    }
}

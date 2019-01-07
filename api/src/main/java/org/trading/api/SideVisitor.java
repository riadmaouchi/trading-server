package org.trading.api;

import org.trading.Side;

public interface SideVisitor<T, R> {

    R visitBuy(T t);

    R visitSell(T t);

    R visitUnknownValue(T t);

    default R visit(Side side, T t) {
        R result;

        switch (side) {
            case BUY:
                result = visitBuy(t);
                break;
            case SELL:
                result = visitSell(t);
                break;
            default:
                result = visitUnknownValue(t);
                break;
        }
        return result;
    }
}
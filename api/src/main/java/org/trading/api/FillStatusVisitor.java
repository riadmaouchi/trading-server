package org.trading.api;


import org.trading.MessageProvider;

public interface FillStatusVisitor< R> {

    R visitFullyFilled();

    R visitPartiallyFilled();

    R visitUnknownValue();

    default R visit(MessageProvider.FillStatus fillStatus) {
        R result;

        switch (fillStatus) {
            case FULLY_FILLED:
                result = visitFullyFilled();
                break;
            case PARTIALLY_FILLED:
                result = visitPartiallyFilled();
                break;
            default:
                result = visitUnknownValue();
                break;
        }
        return result;
    }
}

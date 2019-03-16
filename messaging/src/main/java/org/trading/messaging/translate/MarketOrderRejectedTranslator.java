package org.trading.messaging.translate;

import org.trading.api.event.MarketOrderRejected;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.MARKET_ORDER_REJECTED;

public final class MarketOrderRejectedTranslator {

    private MarketOrderRejectedTranslator() {
    }

    public static void translateTo(Message message, long sequence, MarketOrderRejected marketOrderRejected) {
        message.event = marketOrderRejected;
        message.type = MARKET_ORDER_REJECTED;
    }
}

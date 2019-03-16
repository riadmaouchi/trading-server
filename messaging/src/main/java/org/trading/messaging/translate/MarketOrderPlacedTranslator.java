package org.trading.messaging.translate;

import org.trading.api.event.MarketOrderAccepted;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.MARKET_ORDER_ACCEPTED;

public final class MarketOrderPlacedTranslator {

    private MarketOrderPlacedTranslator() {
    }

    public static void translateTo(Message message, long sequence, MarketOrderAccepted marketOrderPlaced) {
        message.event = marketOrderPlaced;
        message.type = MARKET_ORDER_ACCEPTED;
    }
}

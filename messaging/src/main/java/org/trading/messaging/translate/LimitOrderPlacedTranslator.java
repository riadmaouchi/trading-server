package org.trading.messaging.translate;

import org.trading.api.event.LimitOrderPlaced;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.LIMIT_ORDER_PLACED;

public final class LimitOrderPlacedTranslator {

    private LimitOrderPlacedTranslator() {
    }

    public static void translateTo(Message message, long sequence, LimitOrderPlaced limitOrderPlaced) {
        message.event = limitOrderPlaced;
        message.type = LIMIT_ORDER_PLACED;
    }
}

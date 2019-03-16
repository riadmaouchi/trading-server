package org.trading.messaging.translate;

import org.trading.api.event.LimitOrderAccepted;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.LIMIT_ORDER_ACCEPTED;

public final class LimitOrderPlacedTranslator {

    private LimitOrderPlacedTranslator() {
    }

    public static void translateTo(Message message, long sequence, LimitOrderAccepted limitOrderAccepted) {
        message.event = limitOrderAccepted;
        message.type = LIMIT_ORDER_ACCEPTED;
    }
}

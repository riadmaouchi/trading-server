package org.trading.messaging.translate;

import org.trading.api.message.SubmitOrder;
import org.trading.messaging.Message;

public final class SubmitOrderTranslator {

    private SubmitOrderTranslator() {
    }

    public static void translateTo(Message message, long sequence, SubmitOrder submitOrder) {
        message.event = submitOrder;
        message.type = Message.EventType.SUBMIT_ORDER;
    }
}

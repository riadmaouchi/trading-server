package org.trading.trade.execution.esp.translate;

import org.trading.MessageProvider.SubmitOrder;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.SUBMIT_ORDER;

public final class SubmitOrderTranslator {

    private SubmitOrderTranslator() {
    }

    public static void translateTo(Message message, long sequence, SubmitOrder submitOrder) {
        message.event = submitOrder;
        message.type = SUBMIT_ORDER;
    }
}

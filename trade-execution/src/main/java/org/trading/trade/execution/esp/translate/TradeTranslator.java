package org.trading.trade.execution.esp.translate;

import org.trading.messaging.Message;
import org.trading.trade.execution.esp.domain.Trade;

import static org.trading.messaging.Message.EventType.REQUEST_EXECUTION;

public final class TradeTranslator {

    private TradeTranslator() {
    }

    public static void translateTo(Message message, long sequence, Trade trade) {
        message.event = trade;
        message.type = REQUEST_EXECUTION;
    }
}

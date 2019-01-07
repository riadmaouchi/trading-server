package org.trading.messaging.translate;

import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;

import static org.trading.messaging.Message.EventType.TRADE_EXECUTED;

public final class TradeExecutedTranslator {

    private TradeExecutedTranslator() {
    }

    public static void translateTo(Message message, long sequence, TradeExecuted tradeExecuted) {
        message.event = tradeExecuted;
        message.type = TRADE_EXECUTED;
    }
}
